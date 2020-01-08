// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.grpc;

import static com.google.common.hash.Hashing.sha256;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLException;

import org.joda.time.DateTime;

import com.baidu.acu.pie.AsrServiceGrpc;
import com.baidu.acu.pie.AsrServiceGrpc.AsrServiceStub;
import com.baidu.acu.pie.AudioStreaming;
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
import com.baidu.acu.pie.util.Base64;
import com.baidu.acu.pie.util.DateTimeParser;
import com.google.common.base.Strings;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.internal.IoUtils;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
        log.info("start to recognition, file: {}", audioFile.getAbsoluteFile().getName());

        try {
            byte[] data = Files.readAllBytes(audioFile.toPath());
            return this.syncRecognize(data, requestMetaData);
        } catch (IOException e) {
            log.error("fail to read file", e);
            throw new AsrClientException("fail to read file");
        }
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream) {
        return this.syncRecognize(inputStream, new RequestMetaData());
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream, RequestMetaData requestMetaData) {
        try {
            byte[] data = IoUtils.toByteArray(inputStream);
            return this.syncRecognize(data, requestMetaData);
        } catch (IOException e) {
            log.error("fail to read input stream", e);
            throw new AsrClientException("fail to read input stream");
        }
    }

    @Override
    @SneakyThrows({InterruptedException.class})
    public List<RecognitionResult> syncRecognize(final byte[] data, final RequestMetaData requestMetaData) {
        // prepare streamContext
        final List<RecognitionResult> results = new ArrayList<>();
        final StreamContext streamContext = this.asyncRecognize(new Consumer<RecognitionResult>() {
            @Override
            public void accept(RecognitionResult recognitionResult) {
                results.add(recognitionResult);
            }
        }, requestMetaData);

        // read input stream and send data
        double sleepRatio = requestMetaData.getSleepRatio();
        if (sleepRatio == 0) {
            streamContext.send(data);
        } else {
            final CountDownLatch sendFinishLatch = new CountDownLatch(1);
            final AtomicInteger offset = new AtomicInteger(0);
            final int fragmentSize = streamContext.getFragmentSize();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (offset.get() < data.length && !streamContext.getFinishLatch().finished()) {
                        streamContext.send(Arrays.copyOfRange(
                                data,
                                offset.get(),
                                Math.min(offset.addAndGet(fragmentSize), data.length)));
                    } else {
                        sendFinishLatch.countDown();
                    }
                }
            }, 0L, (long) (sleepRatio * requestMetaData.getSendPerSeconds() * 1000));

            sendFinishLatch.await(); // blocking wait for all data is send to server
            timer.cancel();
        }
        streamContext.complete();

        // wait for recognition finish
        if (!streamContext.await(requestMetaData.getTimeoutMinutes(), TimeUnit.MINUTES)) {
            log.error("Recognition request not finish within {} minutes, maybe the audio is too large",
                    requestMetaData.getTimeoutMinutes());
        }

        log.info("finish recognition request");
        return results;
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
                        if (response.getErrorCode() == 0) {
                            resultConsumer.accept(fromAudioFragmentResponse(
                                    response.getErrorMessage(), response.getAudioFragment()));
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
                .setEnableFlushData(requestMetaData.isEnableFlushData())
                .setProductId(asrConfig.getProduct().getCode())
                .setSamplePointBytes(2)
                .setSendPerSeconds(requestMetaData.getSendPerSeconds())
                .setSleepRatio(requestMetaData.getSleepRatio())
                .setAppName(asrConfig.getAppName())
                .setLogLevel(asrConfig.getLogLevel().getCode())
                .setUserName(asrConfig.getUserName())
                .setExpireTime(expireDateTime)
                .setToken(digestedToken)
                .setVersion(AudioStreaming.ProtoVersion.VERSION_1)
                .setExtraInfo(requestMetaData.getExtraInfo())
                .build();

        Metadata headers = new Metadata();
        String meta_string = Base64.encode(initRequest.toByteArray());
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER), meta_string);
        log.info("init request: \n{}meta_string: {}", initRequest.toString(), meta_string);
        return headers;
    }

    private RecognitionResult fromAudioFragmentResponse(String traceId, AudioStreaming.AudioFragmentResult response) {
        return RecognitionResult.builder()
                .traceId(traceId)
                .serialNum(response.getSerialNum())
                .startTime(DateTimeParser.parseLocalTime(response.getStartTime()))
                .endTime(DateTimeParser.parseLocalTime(response.getEndTime()))
                .result(response.getResult())
                .completed(response.getCompleted())
                .build();
    }

}
