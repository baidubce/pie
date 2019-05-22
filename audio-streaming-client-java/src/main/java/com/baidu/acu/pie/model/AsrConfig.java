// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import org.joda.time.DateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * AsrConfig
 *
 * @author Cynric Shu (cynricshu@gmail.com)
 */
@Builder
@Data
@Slf4j
public class AsrConfig {
    public static final String TITLE_FORMAT = "%-40s\t%-36s\t%-14s\t%-13s\t%s";

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
    private AsrProduct product;

    /**
     * asr客户端的名称，为便于后端查错，请设置一个易于辨识的appName
     */
    @NonNull
    private String appName;

    /**
     * 服务端的日志输出级别
     */
    @Builder.Default
    private AsrServerLogLevel logLevel = AsrServerLogLevel.INFO;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 认证过期时间，使用标准 UTC string(yyyy-MM-dd'T'HH:mm:ss'Z')
     */
    private DateTime expireDateTime;

    /**
     * 和后端 server 建立连接时，用来鉴权的
     */
    private String token;

    @Deprecated
    public AsrConfig serverIp(String serverIp) {
        this.serverIp = serverIp;
        return this;
    }

    @Deprecated
    public AsrConfig serverPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    @Deprecated
    public AsrConfig appName(String appName) {
        this.appName = appName;
        return this;
    }

    @Deprecated
    public AsrConfig product(AsrProduct product) {
        this.product = product;
        return this;
    }

    @Deprecated
    public AsrConfig userName(String userName) {
        this.userName = userName;
        return this;
    }

    @Deprecated
    public AsrConfig password(String password) {
        this.password = password;
        return this;
    }

    @Deprecated
    public AsrConfig token(String token) {
        this.token = token;
        return this;
    }

    @Deprecated
    public AsrConfig expireDateTime(DateTime dateTime) {
        this.expireDateTime = dateTime;
        return this;
    }
}
