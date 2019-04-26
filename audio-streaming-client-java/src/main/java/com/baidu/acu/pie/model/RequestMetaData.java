package com.baidu.acu.pie.model;

import com.baidu.acu.pie.exception.AsrClientException;

import lombok.Getter;

/**
 * RequestMetaData
 * 每次发起请求的时候，可以夹带一些控制参数
 *
 * @author Shu Lingjie
 */
@Getter
public class RequestMetaData {
    /**
     * 是否返回中间翻译结果
     */
    private boolean enableFlushData = true;

    /**
     * 指定每次发送的音频数据包大小，数值越大，识别越快，但准确率可能下降
     */
    private double sendPerSeconds = 0.02;

    /**
     * 用来控制发包大小的倍率，一般不需要修改。
     */
    private double sendPackageRatio = 1.5;

    /**
     * 指定asr服务的识别间隔，数值越小，识别越快，但准确率可能下降。
     */
    private double sleepRatio = 1;

    /**
     * 识别单个文件的最大等待时间，默认10分，最长不能超过120分
     */
    private int timeoutMinutes = 10;

    public static RequestMetaData defaultRequestMeta() {
        return new RequestMetaData();
    }

    public RequestMetaData enableFlushData(boolean enableFlushData) {
        this.enableFlushData = enableFlushData;
        return this;
    }

    public RequestMetaData sendPerSeconds(double sendPerSeconds) {
        this.sendPerSeconds = sendPerSeconds;
        return this;
    }

    public RequestMetaData sendPackageRatio(double sendPackageRatio) {
        this.sendPackageRatio = sendPackageRatio;
        return this;
    }

    public RequestMetaData sleepRatio(double sleepRatio) {
        this.sleepRatio = sleepRatio;
        return this;
    }

    public RequestMetaData timeoutMinutes(int timeoutMinutes) {
        if (timeoutMinutes > 120) {
            throw new AsrClientException("timeoutMinutes should not exceed 120");
        }
        this.timeoutMinutes = timeoutMinutes;
        return this;
    }
}
