// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.grpc;

import com.baidu.acu.pie.AsrServiceGrpc;
import com.baidu.acu.pie.AsrServiceGrpc.AsrServiceStub;
import com.baidu.acu.pie.AudioStreaming;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentResponse;
import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AsrClientGrpcImpl
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Slf4j
public class AsrClientGrpcImpl implements AsrClient {
    private final ManagedChannel managedChannel;
    private final AsrServiceStub asyncStub;
    private AsrConfig asrConfig;

    public AsrClientGrpcImpl(AsrConfig asrConfig) {
        this.asrConfig = asrConfig;
        managedChannel = ManagedChannelBuilder
                .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                .usePlaintext()
                .build();
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                Base64.getEncoder().encodeToString(this.buildInitRequest().toByteArray()));

        asyncStub = MetadataUtils.attachHeaders(AsrServiceGrpc.newStub(managedChannel), headers);
    }

    @Override
    public void shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: {}", e);
        }
    }

    private AudioStreaming.InitRequest buildInitRequest() {
        return AudioStreaming.InitRequest.newBuilder()
                .setEnableLongSpeech(true)
                .setEnableChunk(true)
                .setEnableFlushData(asrConfig.isEnableFlushData())
                .setProductId(asrConfig.getProduct().getCode())
                .setSamplePointBytes(asrConfig.getBitDepth())
                .setSendPerSeconds(asrConfig.getSendPerSeconds())
                .setSleepRatio(asrConfig.getSleepRatio())
                .setAppName(asrConfig.getAppName())
                .setLogLevel(asrConfig.getLogLevel().getCode())
                .build();
    }

    @Override
    public List<RecognitionResult> syncRecognize(Path audioFilePath) {
        log.info("start to recognition, file: {}", audioFilePath.toString());

        final List<RecognitionResult> results = new ArrayList<>();
        CountDownLatch finishLatch = this.sendRequests(prepareRequests(audioFilePath), results);

        try {
            if (!finishLatch.await(asrConfig.getTimeoutMinutes(), TimeUnit.MINUTES)) {
                log.error("Recognition request not finish within {} minutes, maybe the audio is too large",
                        asrConfig.getTimeoutMinutes());
            }
        } catch (InterruptedException e) {
            log.error("error when wait for CountDownLatch: {}", e);
        }

        log.info("finish recognition request");
        return results;
    }

    @Override
    public void asyncRecognize(InputStream audioStream, Consumer<RecognitionResult> resultConsumer) {
        // TODO @cynricshu
        log.info("start to async recognition");

        StreamObserver<AudioFragmentRequest> requestStreamObserver = asyncStub.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse response) {
                        resultConsumer.accept(fromAudioFragmentResponse(response));
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("receive response error: {}", t);
                    }

                    @Override
                    public void onCompleted() {
                        log.info("StreamObserver completed");
                    }
                });

        byte[] data = new byte[this.asrConfig.getProduct().getFragmentSize()];
        int readSize;

        while (true) {
            try {
                if ((readSize = audioStream.read(data)) != -1) {
                    requestStreamObserver.onNext(AudioFragmentRequest.newBuilder()
                            .setAudioData(ByteString.copyFrom(data, 0, readSize))
                            .build());
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("no more data from input stream for 3 times, finsh recognition");
    }

    private List<AudioFragmentRequest> prepareRequests(Path audioFilePath) {
        List<AudioFragmentRequest> requests = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(audioFilePath)) {
            byte[] data = new byte[this.asrConfig.getProduct().getFragmentSize()];
            int readSize;

            while ((readSize = inputStream.read(data)) != -1) {
                requests.add(AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data, 0, readSize))
                        .build()
                );
            }
        } catch (IOException e) {
            log.error("Read audio file failed: {}", e);
        }

        return requests;
    }

    private CountDownLatch sendRequests(List<AudioFragmentRequest> requests, List<RecognitionResult> results) {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<AudioFragmentRequest> requestStreamObserver = asyncStub.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse response) {
                        if (response.getCompleted()) {
                            results.add(fromAudioFragmentResponse(response));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("receive response error: {}", t);
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
            log.error("send request failed: {}", e);
        } finally {
            requestStreamObserver.onCompleted();
        }

        return finishLatch;
    }

    private RecognitionResult fromAudioFragmentResponse(AudioFragmentResponse response) {
        return RecognitionResult.builder()
                .serialNum(response.getSerialNum())
                .errorCode(response.getErrorCode())
                .errorMessage(response.getErrorMessage())
                .startTime(response.getStartTime())
                .endTime(response.getEndTime())
                .result(response.getResult())
                .completed(response.getCompleted())
                .build();
    }
}
