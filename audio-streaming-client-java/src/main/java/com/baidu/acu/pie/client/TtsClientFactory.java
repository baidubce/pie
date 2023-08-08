package com.baidu.acu.pie.client;

import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.grpc.TtsClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.ChannelConfig;
import com.baidu.acu.pie.model.TtsConfig;

/**
 * 类<code>TtsClientFactory</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
public class TtsClientFactory {
    public static TtsClient buildClient(TtsConfig ttsConfig) {
        return new TtsClientGrpcImpl(ttsConfig);
    }

    public static TtsClient buildClient(TtsConfig ttsConfig, ChannelConfig channelConfig) {
        return new TtsClientGrpcImpl(ttsConfig, channelConfig);
    }
}
