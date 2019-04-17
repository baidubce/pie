package com.baidu.acu.pie.model;

import com.baidu.acu.pie.AudioStreaming;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StreamContext {
    private FinishLatch finishLatch;
    private StreamObserver<AudioStreaming.AudioFragmentRequest> sender;
}
