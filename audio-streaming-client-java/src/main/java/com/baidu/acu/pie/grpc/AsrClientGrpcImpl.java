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
import com.baidu.acu.pie.model.*;
import com.baidu.acu.pie.util.Base64;
import com.baidu.acu.pie.util.DateTimeParser;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    public AsrClientGrpcImpl(AsrConfig asrConfig) {
        this(asrConfig, ChannelConfig.builder().build());
    }

    public AsrClientGrpcImpl(AsrConfig asrConfig, ChannelConfig channelConfig) {
        this.asrConfig = asrConfig;
        managedChannel = ManagedChannelBuilder
                .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                .usePlaintext()
                .keepAliveTime(channelConfig.getKeepAliveTime().getTime(),
                        channelConfig.getKeepAliveTime().getTimeUnit())
                .keepAliveTimeout(channelConfig.getKeepAliveTimeout().getTime(),
                        channelConfig.getKeepAliveTimeout().getTimeUnit())
                .build();

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
        return this.getFragmentSize(RequestMetaData.defaultRequestMeta());
    }

    private int getFragmentSize(RequestMetaData requestMetaData) {
        return (int) (asrConfig.getProduct().getSampleRate()
                * asrConfig.getBitDepth()
                * requestMetaData.getSendPerSeconds()
                * requestMetaData.getSendPackageRatio());
    }

    @Override
    public List<RecognitionResult> syncRecognize(File audioFile) {
        return this.syncRecognize(audioFile, RequestMetaData.defaultRequestMeta());
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

        return this.syncRecognize(inputStream);
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream) {
        return this.syncRecognize(inputStream, RequestMetaData.defaultRequestMeta());
    }

    @Override
    public List<RecognitionResult> syncRecognize(InputStream inputStream, RequestMetaData requestMetaData) {
        final List<RecognitionResult> results = new ArrayList<>();

        List<AudioFragmentRequest> requests;
        try {
            requests = this.prepareRequests(inputStream);
        } catch (IOException e) {
            log.error("Read audio file failed: ", e);
            throw new AsrClientException("Read audio file failed");
        }

        CountDownLatch finishLatch = this.sendRequests(requests, requestMetaData, results);

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

    private List<AudioFragmentRequest> prepareRequests(InputStream inputStream) throws IOException {
        List<AudioFragmentRequest> requests = new ArrayList<>();
        byte[] data = new byte[this.getFragmentSize()];
        int readSize;

        while ((readSize = inputStream.read(data)) != -1) {
            requests.add(AudioFragmentRequest.newBuilder()
                    .setAudioData(ByteString.copyFrom(data, 0, readSize))
                    .build()
            );
        }

        return requests;
    }

    private CountDownLatch sendRequests(List<AudioFragmentRequest> requests, RequestMetaData requestMetaData,
                                        final List<RecognitionResult> results) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        AsrServiceStub stubWithMetadata = MetadataUtils.attachHeaders(asyncStub, prepareMetadata(requestMetaData));

        StreamObserver<AudioFragmentRequest> requestStreamObserver = stubWithMetadata.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse response) {
                        if (response.getErrorCode() == 0) {
                            results.add(fromAudioFragmentResponse(response.getAudioFragment()));
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
        return this.asyncRecognize(resultConsumer, RequestMetaData.defaultRequestMeta());
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
                            resultConsumer.accept(fromAudioFragmentResponse(response.getAudioFragment()));
                        } else {
                            finishLatch.fail(new AsrException(response.getErrorCode(), response.getErrorMessage()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        // TODO: 2019-04-28 错误码需要规范一下
                        finishLatch.fail(new AsrException(-2000, t));
                    }

                    @Override
                    public void onCompleted() {
                        finishLatch.finish();
                    }
                }))
                .finishLatch(finishLatch)
                .build();
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
                .setProductId(asrConfig.getProductId())
                .setSamplePointBytes(asrConfig.getBitDepth())
                .setAppName(asrConfig.getAppName())
                .setLogLevel(asrConfig.getLogLevel().getCode())
                .setUserName(asrConfig.getUserName())
                .setExpireTime(expireDateTime)
                .setToken(digestedToken)
                .setEnableFlushData(requestMetaData.isEnableFlushData())
                .setSendPerSeconds(requestMetaData.getSendPerSeconds())
                .setSleepRatio(requestMetaData.getSleepRatio())
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

}
