// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.grpc;

import com.baidu.acu.pie.AsrServiceGrpc;
import com.baidu.acu.pie.AsrServiceGrpc.AsrServiceStub;
import com.baidu.acu.pie.AudioStreaming;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentResponse;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.Consumer;
import com.baidu.acu.pie.exception.AsrClientException;
import com.baidu.acu.pie.exception.AsrException;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.ChannelConfig;
import com.baidu.acu.pie.model.Constants;
import com.baidu.acu.pie.model.FinishLatchImpl;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.retrofit.KafkaHttpClient;
import com.baidu.acu.pie.retrofit.model.KafkaHttpConfig;
import com.baidu.acu.pie.retrofit.model.KafkaHttpRequestBody;
import com.baidu.acu.pie.util.Base64;
import com.baidu.acu.pie.util.DateTimeParser;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.hash.Hashing.sha256;

/**
 * AsrClientGrpcImpl
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
@Slf4j
public class AsrClientGrpcImpl implements AsrClient {
    private final ManagedChannel managedChannel;
    private final AsrServiceStub asyncStub;
    private AsrConfig asrConfig;

    private Map<String, Object> includedInfo;
    private KafkaHttpConfig kafkaHttpConfig;
    private String sessionId;

    public AsrClientGrpcImpl(AsrConfig asrConfig) {
        this(asrConfig, ChannelConfig.builder().build());
    }

    public AsrClientGrpcImpl(AsrConfig asrConfig, ChannelConfig channelConfig) {
        this.asrConfig = asrConfig;

        if (asrConfig.isSslUseFlag()) {
            managedChannel = initSslManagedChannel(asrConfig, channelConfig);
        } else {
            managedChannel = initManagedChannel(asrConfig, channelConfig);
        }

        asyncStub = AsrServiceGrpc.newStub(managedChannel);
    }

    public AsrClientGrpcImpl(AsrConfig asrConfig, KafkaHttpConfig kafkaHttpConfig) {
        this(asrConfig, ChannelConfig.builder().build(), kafkaHttpConfig);
    }

    public AsrClientGrpcImpl(AsrConfig asrConfig, ChannelConfig channelConfig, KafkaHttpConfig kafkaHttpConfig) {
        this.asrConfig = asrConfig;

        if (asrConfig.isSslUseFlag()) {
            managedChannel = initSslManagedChannel(asrConfig, channelConfig);
        } else {
            managedChannel = initManagedChannel(asrConfig, channelConfig);
        }

        asyncStub = AsrServiceGrpc.newStub(managedChannel);
        this.kafkaHttpConfig = kafkaHttpConfig;
    }

    @Override
    public void shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: ", e);
        }
    }

    @Override
    public int getFragmentSize() {
        return this.getFragmentSize(new RequestMetaData());
    }

    @Override
    public int getFragmentSize(RequestMetaData requestMetaData) {
        return (int) (asrConfig.getProduct().getSampleRate()
                * requestMetaData.getSendPackageRatio()
                * requestMetaData.getSendPerSeconds()
                * Constants.DEFAULT_BIT_DEPTH); // bit-depth
    }

    @Override
    public List<RecognitionResult> syncRecognize(File audioFile) {
        return this.syncRecognize(audioFile, new RequestMetaData());
    }

    @Override
    public List<RecognitionResult> syncRecognize(File audioFile, RequestMetaData requestMetaData) {
        log.info("start to recognition, file: {}", audioFile);

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(audioFile);
        } catch (FileNotFoundException e) {
            log.error("AudioFile not exists: ", e);
            throw new AsrClientException("AudioFile not exists");
        }

        return this.syncRecognize(inputStream, requestMetaData);
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream) {
        return this.syncRecognize(inputStream, new RequestMetaData());
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream, RequestMetaData requestMetaData) {
        final List<RecognitionResult> results = new ArrayList<>();

        CountDownLatch finishLatch = this.sendRequests(inputStream, requestMetaData, results);

        try {
            if (!finishLatch.await(requestMetaData.getTimeoutMinutes(), TimeUnit.MINUTES)) {
                log.error("Recognition request not finish within {} minutes, maybe the audio is too large",
                        requestMetaData.getTimeoutMinutes());
            }
        } catch (InterruptedException e) {
            log.error("error when wait for CountDownLatch: ", e);
        }

        log.info("finish recognition request");
        return results;
    }

    private CountDownLatch sendRequests(InputStream inputStream, RequestMetaData requestMetaData,
                                        final List<RecognitionResult> results) {

        List<AudioFragmentRequest> requests = new ArrayList<>();
        byte[] data = new byte[this.getFragmentSize(requestMetaData)];

        try {
            while (inputStream.read(data) != -1) {
                requests.add(AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data))
                        .build()
                );
            }
        } catch (IOException e) {
            log.error("Read audio failed: ", e);
            throw new AsrClientException("Read audio failed");
        }

        final CountDownLatch finishLatch = new CountDownLatch(1);
        AsrServiceStub stubWithMetadata = MetadataUtils.attachHeaders(asyncStub, prepareMetadata(requestMetaData));

        StreamObserver<AudioFragmentRequest> requestStreamObserver = stubWithMetadata.send(
                new StreamObserver<AudioFragmentResponse>() {

                    @Override
                    public void onNext(AudioFragmentResponse response) {
                        if (response.getErrorCode() == 0) {

                            RecognitionResult result = fromAudioFragmentResponse(response.getAudioFragment());
                            if (result.isCompleted()) {
                                sendToServer(result);
                            }
                            results.add(result);
                        } else {
                            log.error("response with error: {}, {}",
                                    response.getErrorCode(), response.getErrorMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("receive response error: ", t);
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("StreamObserver completed");
                        finishLatch.countDown();
                    }
                });

        try {
            for (AudioFragmentRequest message : requests) {
                requestStreamObserver.onNext(message);
            }
        } catch (RuntimeException e) {
            requestStreamObserver.onError(e);
            log.error("send request failed: ", e);
        } finally {
            requestStreamObserver.onCompleted();
        }

        return finishLatch;
    }

    @Override
    public StreamContext asyncRecognize(final Consumer<RecognitionResult> resultConsumer) {
        return this.asyncRecognize(resultConsumer, new RequestMetaData());
    }

    @Override
    public StreamContext asyncRecognize(final Consumer<RecognitionResult> resultConsumer,
                                        RequestMetaData requestMetaData) {
        final FinishLatchImpl finishLatch = new FinishLatchImpl();
        AsrServiceStub stubWithMetadata = MetadataUtils.attachHeaders(asyncStub, prepareMetadata(requestMetaData));

        return StreamContext.builder()
                .sender(stubWithMetadata.send(new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse response) {

                        RecognitionResult result = fromAudioFragmentResponse(response.getAudioFragment());
                        if (response.getErrorCode() == 0) {
                            if (result.isCompleted()) {
                                sendToServer(result);
                            }

                            resultConsumer.accept(fromAudioFragmentResponse(response.getAudioFragment()));
                        } else {
                            finishLatch.fail(new AsrException(response.getErrorCode(), response.getErrorMessage()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("error in response observer: ", t);
                        // TODO: 2019-04-28 错误码需要规范一下
                        finishLatch.fail(new AsrException(-2000, t));
                    }

                    @Override
                    public void onCompleted() {
                        log.info("response observer complete");
                        finishLatch.finish();
                    }
                }))
                .finishLatch(finishLatch)
                .fragmentSize(getFragmentSize(requestMetaData))
                .build();
    }

    private ManagedChannel initManagedChannel(AsrConfig asrConfig, ChannelConfig channelConfig) {

        return ManagedChannelBuilder
                .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                .usePlaintext()
                .keepAliveTime(channelConfig.getKeepAliveTime().getTime(),
                        channelConfig.getKeepAliveTime().getTimeUnit())
                .keepAliveTimeout(channelConfig.getKeepAliveTimeout().getTime(),
                        channelConfig.getKeepAliveTimeout().getTimeUnit())
                .build();
    }

    private ManagedChannel initSslManagedChannel(AsrConfig asrConfig, ChannelConfig channelConfig) {
        try {
            return NettyChannelBuilder
                    .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                    .keepAliveTime(channelConfig.getKeepAliveTime().getTime(),
                            channelConfig.getKeepAliveTime().getTimeUnit())
                    .keepAliveTimeout(channelConfig.getKeepAliveTimeout().getTime(),
                            channelConfig.getKeepAliveTimeout().getTimeUnit())
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(GrpcSslContexts.forClient()
                            .trustManager(new File(asrConfig.getSslPath())).build())
                    .build();
        } catch (SSLException e) {
            throw new AsrClientException("build ssl client failed");
        }
    }

    private Metadata prepareMetadata(RequestMetaData requestMetaData) {
        String digestedToken;
        String expireDateTime;

        if (Strings.isNullOrEmpty(asrConfig.getToken())) {
            expireDateTime = DateTimeParser.toUTCString(DateTime.now().plusMinutes(30));
            String rawToken = asrConfig.getUserName() + asrConfig.getPassword() + expireDateTime;
            digestedToken = sha256().hashString(rawToken, StandardCharsets.UTF_8).toString();
        } else {
            // 如果传入了 token，必须同时传入相应的 expireDateTime
            if (asrConfig.getExpireDateTime() == null) {
                throw new AsrClientException("Neither `token` nor `expireDateTime` should be Null");
            } else {
                expireDateTime = DateTimeParser.toUTCString(asrConfig.getExpireDateTime());
            }

            digestedToken = asrConfig.getToken();
        }

        AudioStreaming.InitRequest initRequest = AudioStreaming.InitRequest.newBuilder()
                .setEnableLongSpeech(true)
                .setEnableChunk(true)
                .setProductId(asrConfig.getProduct().getCode())
                .setSamplePointBytes(2)
                .setAppName(asrConfig.getAppName())
                .setLogLevel(asrConfig.getLogLevel().getCode())
                .setUserName(asrConfig.getUserName())
                .setExpireTime(expireDateTime)
                .setToken(digestedToken)
                .setSendPerSeconds(requestMetaData.getSendPerSeconds())
                .setEnableFlushData(requestMetaData.isEnableFlushData())
                .setSleepRatio(requestMetaData.getSleepRatio())
                .setExtraInfo(requestMetaData.getExtraInfo())
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                Base64.encode(initRequest.toByteArray()));
        return headers;
    }

    private RecognitionResult fromAudioFragmentResponse(AudioStreaming.AudioFragmentResult response) {
        return RecognitionResult.builder()
                .serialNum(response.getSerialNum())
                .startTime(DateTimeParser.parseLocalTime(response.getStartTime()))
                .endTime(DateTimeParser.parseLocalTime(response.getEndTime()))
                .result(response.getResult())
                .completed(response.getCompleted())
                .build();
    }

    @Override
    public void setIncludedInfo(Map<String, Object> info) {
        this.includedInfo = info;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private long timeToMillions(LocalTime time) {
        log.trace("howr: {}, minute: {}, seconde: {}, millis: {}",
                time.getHourOfDay(),
                time.getMinuteOfHour(),
                time.getSecondOfMinute(),
                time.getMillisOfSecond());
        return (time.getHourOfDay() * 3600 +
                time.getMinuteOfHour() * 60 +
                time.getSecondOfMinute()) * 1000 +
                time.getMillisOfSecond();
    }

    private void sendToServer(RecognitionResult result) {
        KafkaHttpRequestBody requestBody = new KafkaHttpRequestBody();
        KafkaHttpClient client = new KafkaHttpClient(kafkaHttpConfig.getServer(), kafkaHttpConfig.getPort());

        requestBody.setRecog_result(result.getResult());
        requestBody.setBegin_time("" + timeToMillions(result.getStartTime()));
        requestBody.setEnd_time("" + timeToMillions(result.getEndTime()));
        requestBody.setRecog_type(1);

        requestBody.setSession_param(includedInfo);
        requestBody.setSession_id(sessionId);

        client.sendRequest(requestBody);
    }
}
