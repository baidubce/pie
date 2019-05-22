package com.baidu.acu.pie.model;

import com.baidu.acu.pie.exception.AsrClientException;

import lombok.Data;
import lombok.ToString;

/**
 * RequestMetaData
 * 每次发起请求的时候，可以夹带一些控制参数
 *
 * @author Shu Lingjie
 */
@Data
@ToString
public class RequestMetaData {
    /**
     * 是否返回中间翻译结果
     */
    private boolean enableFlushData = true;

    /**
     * 每一帧发送0.02秒的音频数据，本参数 99% 情况下不用修改。
     * 只有在异步准实时场景下，和一些上游系统做对接的时候，可能需要修改。
     * 比如上游的数据是每隔 10ms 发送过来，那么此时就可以将改参数改成 0.01
     */
    private double sendPerSeconds = 0.02;

    /**
     * 用来控制发包速率，数值越大，识别速度越快，但准确率可能下降。
     * 一般不建议修改。
     */
    private double sendPackageRatio = 1.0;

    /**
     * 指定asr服务的识别间隔，数值越小，识别速度越快，但准确率可能下降。
     * 一般不建议修改。
     */
    private double sleepRatio = 1.0;

    /**
     * 识别单个文件的最大等待时间，默认10分，最长不能超过120分
     */
    private int timeoutMinutes = 10;

    public void setTimeoutMinutes(int timeoutMinutes) {
        if (timeoutMinutes > 120) {
            throw new AsrClientException("timeoutMinutes should not exceed 120");
        }
        this.timeoutMinutes = timeoutMinutes;
    }

    @Deprecated
    public RequestMetaData enableFlushData(boolean enableFlushData) {
        this.enableFlushData = enableFlushData;
        return this;
    }

    @Deprecated
    public RequestMetaData sendPerSeconds(double sendPerSeconds) {
        this.sendPerSeconds = sendPerSeconds;
        return this;
    }

    @Deprecated
    public RequestMetaData sendPackageRatio(double sendPackageRatio) {
        this.sendPackageRatio = sendPackageRatio;
        return this;
    }

    @Deprecated
    public RequestMetaData sleepRatio(double sleepRatio) {
        this.sleepRatio = sleepRatio;
        return this;
    }

    @Deprecated
    public RequestMetaData timeoutMinutes(int timeoutMinutes) {
        if (timeoutMinutes > 120) {
            throw new AsrClientException("timeoutMinutes should not exceed 120");
        }
        this.timeoutMinutes = timeoutMinutes;
        return this;
    }

}
