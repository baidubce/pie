// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.baidu.acu.pie.exception.AsrException;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;

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
     * @param file 音频文件本身
     */
    List<RecognitionResult> syncRecognize(File file) throws AsrException;

    /**
     * 同步识别，输入一个音频文件的 inputstream，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param inputStream
     */
    List<RecognitionResult> syncRecognize(InputStream inputStream) throws AsrException;

    /**
     * 同步识别，输入一个音频文件，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param file 音频文件本身
     */
    List<RecognitionResult> syncRecognize(File file, RequestMetaData requestMetaData) throws AsrException;

    /**
     * 同步识别，输入一个音频文件的 inputstream，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param inputStream
     */
    List<RecognitionResult> syncRecognize(InputStream inputStream, RequestMetaData requestMetaData) throws AsrException;

    /**
     * 同步识别，输入一个音频文件的 byte[]，线程会进入等待，直到识别完毕，返回结果
     * 通常用于对实时性要求不高的场景，如离线语音分析
     *
     * @param data
     */
    List<RecognitionResult> syncRecognize(byte[] data, RequestMetaData requestMetaData) throws AsrException;

    /**
     * 异步识别，输入一个语音流，会准实时返回每个句子的结果
     * 用于对实时性要求较高的场景，如会议记录
     *
     * @param resultConsumer 回调函数处理异步返回的识别结果
     *
     * @return StreamContext 流的上下文信息，通过这个结构体写入和读取数据
     */
    StreamContext asyncRecognize(final Consumer<RecognitionResult> resultConsumer);

    /**
     * 异步识别，输入一个语音流，会准实时返回每个句子的结果
     * 用于对实时性要求较高的场景，如会议记录
     *
     * @param resultConsumer 回调函数处理异步返回的识别结果
     *
     * @return StreamContext 流的上下文信息，通过这个结构体写入和读取数据
     */
    StreamContext asyncRecognize(final Consumer<RecognitionResult> resultConsumer, RequestMetaData requestMetaData);

    /**
     * 异步识别的时候，需要用户手动调用发送逻辑。
     * 在发送的时候，需要设置发包大小，该方法返回最佳发包大小。
     */
    int getFragmentSize();

    /**
     * 异步识别的时候，需要用户手动调用发送逻辑。
     * 在发送的时候，需要设置发包大小，该方法返回最佳发包大小。
     */
    int getFragmentSize(RequestMetaData requestMetaData);

    void shutdown();
}
