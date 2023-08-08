package com.baidu.acu.pie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * 类<code>TtsRequest</code>用于：
 *
 * @author Xia Shuai(xiashuai01@baidu.com)
 * @version 1.0
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TtsRequest {
    private String lan = "zh";
    private String cuid = "xxx";
    private int per = 5106;
    private int ctp = 10;
    private int aue = 4;
    private int pdt = 993;
    private int vol = 5;
    private int pit = 5;
    private int spd = 5;
    private int xml = 0;

    private String sk = "";
    private long sendTimestamp = System.currentTimeMillis();
    private int sequenceNum;
    private Map<String, String> extraParams = new HashMap<>();

    /**
     * 识别单个文件的最大等待时间，默认10分，最长不能超过120分
     */
    private int timeoutMinutes = 120;


}
