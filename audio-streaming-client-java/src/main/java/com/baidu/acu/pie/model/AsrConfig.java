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
    public static final String TITLE_FORMAT_WITH_TIME = "%-25s\t%-9s\t%-6s\t%-10s\t%-10s\t%-9s\t%s";
    public static final String TITLE_FORMAT = "%-6s\t%-10s\t%-10s\t%-9s\t%s";

    /**
     * asr流式服务器的地址，私有化版本请咨询供应商
     */
    @NonNull
    private String serverIp;
    /**
     * asr流式服务的端口，私有化版本请咨询供应商
     */
    private int serverPort;
    /**
     * asr识别服务的产品类型，私有化版本请咨询供应商
     */
    @NonNull
    private AsrProduct product;
    /**
     * asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
     */
    @NonNull
    private String appName;
    private AsrServerLogLevel logLevel = AsrServerLogLevel.INFO;
    /**
     *
     */
    private boolean enableFlushData = true;
    /**
     * do not change this
     */
    private int bitDepth = 2;
    /**
     * 指定每次发送的音频数据包大小，通常不需要修改
     */
    private double sendPerSeconds = 0.16;
    /**
     * 指定asr服务的识别间隔，通常不需要修改
     */
    private int sleepRatio = 1;

    /**
     * 识别单个文件的最大等待时间，默认10分，最长不能超过120分
     */
    private int timeoutMinutes = 10;

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

    public AsrConfig timeoutMinutes(int timeoutMinutes) {
        if (timeoutMinutes > 120) {
            timeoutMinutes = 60;
        }
        this.timeoutMinutes = timeoutMinutes;
        return this;
    }
}
