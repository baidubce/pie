// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.grpc.AsrServiceGrpc;
import com.baidu.acu.pie.grpc.AsrServiceGrpc.AsrServiceStub;
import com.baidu.acu.pie.grpc.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.grpc.AudioStreaming.AudioFragmentResponse;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * AsrClient
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Slf4j
public class AsrClientImpl implements AsrClient {
    private final ManagedChannel managedChannel;
    private final AsrServiceStub asyncStub;
    private AsrConfig asrConfig;

    public AsrClientImpl(AsrConfig asrConfig) {
        this.asrConfig = asrConfig;
        managedChannel = ManagedChannelBuilder
                .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                .usePlaintext()
                .build();
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                Base64.getEncoder().encodeToString(asrConfig.buildInitRequest().toByteArray()));

        asyncStub = MetadataUtils.attachHeaders(AsrServiceGrpc.newStub(managedChannel), headers);
    }

    public void shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: {}", e.getMessage());
        }
    }

    @Override
    public List<RecognitionResult> recognizeAudioFile(Path audioFilePath) {
        log.info("start recognition request, file: {}", audioFilePath.toString());

        List<RecognitionResult> results = new ArrayList<>();
        CountDownLatch finishLatch = this.sendMessages(prepareRequestFromAudioFile(audioFilePath), results);

        try {
            if (!finishLatch.await(60, TimeUnit.MINUTES)) {
                log.error("Recognition request can not finish within 60 minutes, maybe the audio is too large");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("finish recognition request");
        return results;
    }

    private List<AudioFragmentRequest> prepareRequestFromAudioFile(Path audioFilePath) {
        List<AudioFragmentRequest> requests = new ArrayList<>();

        try {
            InputStream inputStream = Files.newInputStream(audioFilePath);

            byte[] data = new byte[this.asrConfig.getProduct().getFragmentSize()];
            int readSize;

            while ((readSize = inputStream.read(data)) != -1) {
                requests.add(AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data, 0, readSize))
                        .build()
                );
            }
        } catch (IOException e) {
            log.error("Read audio file failed: {} ", e.getMessage());
        }

        return requests;
    }

    private CountDownLatch sendMessages(List<AudioFragmentRequest> messages, List<RecognitionResult> results) {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<AudioFragmentRequest> requestStreamObserver = asyncStub.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse value) {
                        results.add(RecognitionResult.builder()
                                .completed(value.getCompleted())
                                .errorCode(value.getErrorCode())
                                .errorMessage(value.getErrorMessage())
                                .startTime(value.getStartTime())
                                .endTime(value.getEndTime())
                                .build());
                        System.out.println(String.format(AsrConfig.TITLE_FORMAT,
                                Instant.now().toString(),
                                value.getCompleted(),
                                value.getErrorCode(),
                                value.getErrorMessage(),
                                value.getStartTime(),
                                value.getEndTime(),
                                value.getResult()));
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error(t.getMessage());
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("complete");
                        finishLatch.countDown();
                    }
                });

        try {
            for (AudioFragmentRequest message : messages) {
                requestStreamObserver.onNext(message);
            }
        } catch (RuntimeException e) {
            requestStreamObserver.onError(e);
            e.printStackTrace();
        } finally {
            requestStreamObserver.onCompleted();
        }

        return finishLatch;
    }
}
