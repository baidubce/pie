package com.baidu.acu.pie.model.result;

import lombok.Data;

/**
 * 音频文件返回结果
 */
@Data
public class MediaResult {
    private String asrResult;
    private boolean isCompleted;
    private boolean isFinished;
}
