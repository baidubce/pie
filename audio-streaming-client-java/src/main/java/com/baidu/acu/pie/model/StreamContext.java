package com.baidu.acu.pie.model;

import com.baidu.acu.pie.AudioStreaming;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamContext {
    private FinishLatch finishLatch;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> sender;

    public void send(byte[] data) {
        this.sender.onNext(
                AudioStreaming.AudioFragmentRequest.newBuilder()
                        .setAudioData(ByteString.copyFrom(data))
                        .build());
    }

    public void complete() {
        this.sender.onCompleted();
    }
}
