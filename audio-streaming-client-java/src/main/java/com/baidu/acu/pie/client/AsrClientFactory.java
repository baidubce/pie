// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.client;

import com.baidu.acu.pie.grpc.AsrClientGrpcImpl;
import com.baidu.acu.pie.model.AsrConfig;

/**
 * AsrClientFactory
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
public class AsrClientFactory {
    public static AsrClient buildClient(AsrConfig asrConfig) {
        return new AsrClientGrpcImpl(asrConfig);
    }
}
