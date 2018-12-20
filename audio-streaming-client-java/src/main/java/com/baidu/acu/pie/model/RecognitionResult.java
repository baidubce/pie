// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Builder;
import lombok.Data;

/**
 * 一个RecognitionResult对象就代表了一个句子的识别结果
 * ASR会通过VAD对音频进行切分，每次返回一个完整的句子识别结果
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Data
@Builder
public class RecognitionResult {
    /**
     * 0表示没有错误
     */
    private int errorCode;
    private String errorMessage;
    /**
     * 句子的开始时间，格式是 mm:ss.zzz
     */
    private String startTime;
    /**
     * 句子的结束时间，格式是 mm:ss.zzz
     */
    private String endTime;
    /**
     * 识别出来的整句结果
     */
    private String result;
    /**
     * 请求唯一标识，用于查错
     */
    private String serialNum;
}
