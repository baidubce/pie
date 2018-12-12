// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baidu.acu.pie.AsrServiceGrpc.AsrServiceStub;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.AudioStreaming.AudioFragmentResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

/**
 * Client
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class AsrClient {
    private final ManagedChannel managedChannel;
    private final AsrServiceStub asyncStub;

    public AsrClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public AsrClient(ManagedChannelBuilder<?> channelBuilder) {
        managedChannel = channelBuilder.build();
        //        ClientInterceptor clientInterceptor = new AsrClientInterceptor();
        //        Channel channel = ClientInterceptors.intercept(managedChannel, clientInterceptor);
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("audio_meta", Metadata.ASCII_STRING_MARSHALLER),
                Base64.getEncoder().encodeToString(Constants.INIT_REQUEST.toByteArray()));

        asyncStub = MetadataUtils.attachHeaders(AsrServiceGrpc.newStub(managedChannel), headers);
    }

    public void shutdown() throws InterruptedException {
        managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public CountDownLatch sendAllData(List<AudioFragmentRequest> messages) {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<AudioFragmentRequest> requestStreamObserver = asyncStub.send(
                new StreamObserver<AudioFragmentResponse>() {
                    @Override
                    public void onNext(AudioFragmentResponse value) {
                        System.out.println(String.format(Constants.TITLE_FORMAT,
                                Instant.now().toString(),
                                value.getCompleted(),
                                value.getErrorCode(),
                                value.getErrorMessage(),
                                value.getStartTime(),
                                value.getEndTime(),
                                value.getResult()
                        ));
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("complete");
                        finishLatch.countDown();
                    }
                });

        try {
            System.out.println(String.format(Constants.TITLE_FORMAT,
                    "time",
                    "completed",
                    "err_no",
                    "err_message",
                    "start_time",
                    "end_time",
                    "result"));
            System.out.println(Instant.now() + "\tstart to sendAllData data");
            for (AudioFragmentRequest message : messages) {
                requestStreamObserver.onNext(message);
            }
        } catch (RuntimeException e) {
            requestStreamObserver.onError(e);
            e.printStackTrace();
        }

        requestStreamObserver.onCompleted();

        return finishLatch;
    }
}
