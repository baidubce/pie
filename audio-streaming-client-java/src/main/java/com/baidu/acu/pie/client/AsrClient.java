// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import com.baidu.acu.pie.model.RecognitionResult;

/**
 * AsrClient
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
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
     * 异步识别，输入一个语音流，会准实时返回每个句子的结果
     * 用于对实时性要求较高的场景，如会议记录
     *
     * @param audioStream
     * @param resultConsumer
     * @return CountDownLatch，来自jdk1.5标准库，具体用法请参见 java doc
     */
    CountDownLatch asyncRecognize(InputStream audioStream, Consumer<RecognitionResult> resultConsumer);

    void shutdown();
}
