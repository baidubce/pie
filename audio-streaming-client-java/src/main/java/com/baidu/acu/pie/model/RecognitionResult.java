// Copyright (C) 2018 Baidu Inc. All rights reserved.

package com.baidu.acu.pie.model;

import lombok.Builder;
import lombok.Data;

/**
 * RecognitionResult
 *
 * @author Shu Lingjie(shulingjie@baidu.com)
 */
@Data
@Builder
public class RecognitionResult {
    private int errorCode;
    private String errorMessage;
    private String startTime;
    private String endTime;
    private String result;
    private boolean completed;
}
