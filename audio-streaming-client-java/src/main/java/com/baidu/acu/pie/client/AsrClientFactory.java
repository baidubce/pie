// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;

/**
 * AsrClientFactory
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
public class AsrClientFactory {
    public static AsrClient buildClient(AsrConfig asrConfig) {
        return new AsrClientGrpcImpl(asrConfig);
    }
}
