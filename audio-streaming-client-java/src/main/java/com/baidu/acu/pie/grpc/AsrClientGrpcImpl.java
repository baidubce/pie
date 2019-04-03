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
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.util.Base64;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.baidu.acu.pie.model.Constants.ASR_RECOGNITION_RESULT_TIME_FORMAT;

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
        this.asrConfig = asrConfig;
        managedChannel = ManagedChannelBuilder
                .forAddress(asrConfig.getServerIp(), asrConfig.getServerPort())
                .usePlaintext()
                .build();
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                Base64.encode(this.buildInitRequest().toByteArray()));
//                DatatypeConverter.printBase64Binary(this.buildInitRequest().toByteArray()));
//                Base64.getEncoder().encodeToString(this.buildInitRequest().toByteArray()));

        asyncStub = MetadataUtils.attachHeaders(AsrServiceGrpc.newStub(managedChannel), headers);
    }

    @Override
    public void shutdown() {
        try {
            managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown failed: ", e);
        }
    }

    private AudioStreaming.InitRequest buildInitRequest() {
        return AudioStreaming.InitRequest.newBuilder()
                .setEnableLongSpeech(true)
                .setEnableChunk(true)
                .setEnableFlushData(asrConfig.isEnableFlushData())
                .setProductId(asrConfig.getProductId())
                .setSamplePointBytes(asrConfig.getBitDepth())
                .setSendPerSeconds(asrConfig.getSendPerSeconds())
                .setSleepRatio(asrConfig.getSleepRatio())
                .setAppName(asrConfig.getAppName())
                .setLogLevel(asrConfig.getLogLevel().getCode())
                .build();
    }

    @Override
    public int getFragmentSize() {
        return (int) (asrConfig.getSendPerSeconds() * asrConfig.getProduct().getSampleRate() * asrConfig.getBitDepth() * 1.5);
    }

    @Override
    public List<RecognitionResult> syncRecognize(Path audioFilePath) {
        return this.syncRecognize(audioFilePath.toFile());
    }

    @Override
    public List<RecognitionResult> syncRecognize(File audioFile) {
        log.info("start to recognition, file: {}", audioFile);

        final List<RecognitionResult> results = new ArrayList<>();
        CountDownLatch finishLatch = this.sendRequests(prepareRequests(audioFile), results);

        try {
            if (!finishLatch.await(asrConfig.getTimeoutMinutes(), TimeUnit.MINUTES)) {
                log.error("Recognition request not finish within {} minutes, maybe the audio is too large",
                        asrConfig.getTimeoutMinutes());
            }
        } catch (InterruptedException e) {
            log.error("error when wait for CountDownLatch: ", e);
        }

        log.info("finish recognition request");
        return results;
    }

    @Override
    public StreamObserver<AudioFragmentRequest> asyncRecognize(
            final Consumer<RecognitionResult> resultConsumer,
            final CountDownLatch finishLatch) {

        return asyncStub.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse response) {
                        resultConsumer.accept(fromAudioFragmentResponse(response));
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
    }

    private List<AudioFragmentRequest> prepareRequests(File audioFile) {
        try (InputStream inputStream = new FileInputStream(audioFile)) {
            return prepareRequests(inputStream);
        } catch (IOException e) {
            log.error("Read audio file failed: ", e);
            throw new AsrClientException("Read audio file failed");
        }
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

    private CountDownLatch sendRequests(List<AudioFragmentRequest> requests, final List<RecognitionResult> results) {
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

    private RecognitionResult fromAudioFragmentResponse(AudioFragmentResponse response) {
        return RecognitionResult.builder()
                .serialNum(response.getSerialNum())
                .errorCode(response.getErrorCode())
                .errorMessage(response.getErrorMessage())
                .startTime(parseLocalTime(response.getStartTime()))
                .endTime(parseLocalTime(response.getEndTime()))
                .result(response.getResult())
                .completed(response.getCompleted())
                .build();
    }

    private LocalTime parseLocalTime(String time) {
        String toBeParsed;

        if (time.matches("\\d{2}:\\d{2}\\.\\d{2}")) { // mm:ss.SS like 01:00.40
            toBeParsed = "00:" + time + "0";
        } else if (time.matches("\\d{2}:\\d{2}\\.\\d{3}")) { // mm:ss.SSS without HH:
            toBeParsed = "00:" + time;
        } else {
            toBeParsed = time;
        }

        DateTimeFormatter asrRecognitionResultTimeFormatter =
                DateTimeFormat.forPattern(ASR_RECOGNITION_RESULT_TIME_FORMAT);

        LocalTime ret = LocalTime.MIDNIGHT;

        try {
            ret = LocalTime.parse(toBeParsed, asrRecognitionResultTimeFormatter);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            log.warn("parse time failed, the time string from asr sdk is : {}, exception: ", time, e);
        }
        return ret;
    }
}
