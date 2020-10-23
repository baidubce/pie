package com.baidu.acu.pie.model.result;

import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.utils.LocalTimeUtil;
import lombok.Data;

/**
 * asr返回结果
 */
@Data
public class AsrResult {
    private String startTime;
    private String endTime;
    private String serialNum;
    private String result;
    private boolean completed;

    public static AsrResult fromRecogniseResult(RecognitionResult recognitionResult) {
        AsrResult asrResult = new AsrResult();
        asrResult.setStartTime(LocalTimeUtil.parseLocalTimeToSeconds(recognitionResult.getStartTime()) + "");
        asrResult.setEndTime(LocalTimeUtil.parseLocalTimeToSeconds(recognitionResult.getEndTime()) + "");
        asrResult.setResult(recognitionResult.getResult());
        asrResult.setSerialNum(recognitionResult.getSerialNum());
        asrResult.setCompleted(recognitionResult.isCompleted());

        return asrResult;

    }

}
