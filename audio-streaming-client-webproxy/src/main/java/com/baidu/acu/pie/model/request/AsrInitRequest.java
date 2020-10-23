package com.baidu.acu.pie.model.request;

import lombok.Data;

/**
 * AsrInitRequest
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2020/10/22 9:06 下午
 */
@Data
public class AsrInitRequest {
    private boolean enableFlushData;

    private String productId;
    private int samplePointBytes = 1;

    private double sendPerSeconds = 0.02;
    private double sleepRatio = 1;

    private String appName = "ws";

    private String userName;
    private String password;
}
