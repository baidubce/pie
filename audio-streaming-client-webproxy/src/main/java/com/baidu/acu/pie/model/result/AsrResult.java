package com.baidu.acu.pie.model.result;

import lombok.Data;

/**
 * asr返回结果
 */
@Data
public class AsrResult {
    private String asrResult;
    private boolean isCompleted;
    private boolean isFinished;
}
