// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.ChannelConfig;

/**
 * AsrClientFactory
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class AsrClientFactory {
    public static AsrClient buildClient(AsrConfig asrConfig) {
        return new AsrClientGrpcImpl(asrConfig);
    }

    public static AsrClient buildClient(AsrConfig asrConfig, ChannelConfig channelConfig) {
        return new AsrClientGrpcImpl(asrConfig, channelConfig);
    }
}
