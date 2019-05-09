package com.baidu.acu.pie.model;

import com.baidu.acu.pie.AudioStreaming;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

@Getter
@Builder
public class StreamContext implements FinishLatch {
    @Delegate(types = FinishLatch.class)
    private FinishLatch finishLatch;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> sender;
    private int fragmentSize;

    public void send(byte[] data) {
        for (int i = 0; i * fragmentSize < data.length; i++) {
            this.sender.onNext(
                    AudioStreaming.AudioFragmentRequest.newBuilder()
                            .setAudioData(ByteString.copyFrom(data, i * fragmentSize,
                                    Math.min(fragmentSize, data.length - i * fragmentSize)))
                            .build());
        }
    }

    public void complete() {
        this.sender.onCompleted();
    }
}
