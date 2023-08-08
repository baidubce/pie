package com.baidu.acu.pie.model;

import com.baidu.acu.pie.AudioStreaming;
import io.grpc.stub.StreamObserver;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * 类<code>TtsStreamContext</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
@Getter
@Builder
public class TtsStreamContext implements FinishLatch {
    @Delegate(types = FinishLatch.class)
    private FinishLatch finishLatch;
}
