// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Getter;
import lombok.NonNull;

/**
 * AsrConfig
 * 不提供Setter，构造时就必须将所有参数设置好。
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Getter
public class AsrConfig {
    public static final String TITLE_FORMAT = "%-25s\t%-9s\t%-6s\t%-10s\t%-10s\t%-9s\t%s";

    @NonNull
    private String serverIp;
    private int serverPort;
    @NonNull
    private AsrProduct product;
    @NonNull
    private String appName;
    private AsrServerLogLevel logLevel = AsrServerLogLevel.INFO;
    private boolean enableFlushData = true; // you can try false
    private int bitDepth = 2;
    private double sendPerSeconds = 0.16;
    private int sleepRatio = 1;

    public AsrConfig serverIp(String serverIp) {
        this.serverIp = serverIp;
        return this;
    }

    public AsrConfig serverPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public AsrConfig appName(String appName) {
        this.appName = appName;
        return this;
    }

    public AsrConfig product(AsrProduct product) {
        this.product = product;
        return this;
    }
}
