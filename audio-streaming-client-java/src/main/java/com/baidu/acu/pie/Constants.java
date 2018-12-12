// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import com.baidu.acu.pie.AudioStreaming.InitRequest;

/**
 * Constants
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class Constants {
    public static final String SERVER_IP_ADDR = "180.76.107.131";
    public static final int SERVER_IP_PORT = 8051;
    public static final String AUDIO_FILE_PATH = "testaudio/bj8k.wav";
    //    public static final String AUDIO_FILE_PATH = "audio-streaming-client/1.wav";
    public static final String TITLE_FORMAT = "%-25s\t%-9s\t%-6s\t%-10s\t%-10s\t%-9s\t%s";

    public static final InitRequest INIT_REQUEST = InitRequest.newBuilder()
            .setEnableLongSpeech(true)
            .setEnableChunk(true)
            .setEnableFlushData(true) // you can try false
            .setProductId("1903")
            .setSamplePointBytes(2)
            .setSendPerSeconds(0.16)
            .setSleepRatio(1)
            .setAppName("cynric_asr_client")
            .setLogLevel(4)
            .build();
}
