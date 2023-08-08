package com.baidu.acu.pie.client;

import com.baidu.acu.pie.exception.GlobalException;
import com.baidu.acu.pie.model.TtsRequest;
import com.baidu.acu.pie.model.TtsStreamContext;

/**
 * 类<code>TtsClient</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
public interface TtsClient {
    byte[] syncToSpeech(String text) throws GlobalException;

    byte[] syncToSpeech(String text, TtsRequest request) throws GlobalException;

    void syncToSpeech(String text, String filepath) throws GlobalException;

    void syncToSpeech(String text, String filepath, TtsRequest request) throws GlobalException;

    TtsStreamContext asyncToSpeech(String text, final Consumer<byte[]> resultConsumer);

    TtsStreamContext asyncToSpeech(String text, final Consumer<byte[]> resultConsumer, TtsRequest request);

    void shutdown();
}
