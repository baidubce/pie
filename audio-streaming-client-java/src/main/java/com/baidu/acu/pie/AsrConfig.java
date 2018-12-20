// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie;

import com.baidu.acu.pie.grpc.AudioStreaming;

import lombok.Builder;
import lombok.Getter;

/**
 * AsrConfig
 * 不提供Setter，构造是就必须将所有参数设置好。
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Getter
@Builder
public class AsrConfig {
    private String serverIp;
    private int serverPort;
    private String productId;

    public AudioStreaming.InitRequest buildInitRequest() {

        return AudioStreaming.InitRequest.newBuilder()
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
}
