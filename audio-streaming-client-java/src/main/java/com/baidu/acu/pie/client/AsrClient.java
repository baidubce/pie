// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import com.baidu.acu.pie.AudioStreaming.AudioFragmentRequest;
import com.baidu.acu.pie.model.RecognitionResult;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * AsrClient
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public interface AsrClient {
    /**
     * 同步识别，输入一个音频文件，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param audioFilePath 音频文件的路径
     */
    List<RecognitionResult> syncRecognize(Path audioFilePath);

    /**
     * 同步识别，输入一个音频文件，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param file 音频文件本身
     */
    List<RecognitionResult> syncRecognize(File file);

    /**
     * 异步识别，输入一个语音流，会准实时返回每个句子的结果
     * 用于对实时性要求较高的场景，如会议记录
     *
     * @param resultConsumer
     * @return CountDownLatch，来自jdk1.5标准库，具体用法请参见 java doc
     */
    StreamObserver<AudioFragmentRequest> asyncRecognize(
            Consumer<RecognitionResult> resultConsumer,
            CountDownLatch finishLatch);

    /**
     * 异步识别的时候，需要用户手动调用发送逻辑。
     * 在发送的时候，需要设置发包大小，该方法返回最佳发包大小。
     */
    int getFragmentSize();

    void shutdown();
}
