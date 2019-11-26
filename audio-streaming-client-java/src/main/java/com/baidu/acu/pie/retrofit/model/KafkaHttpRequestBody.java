package com.baidu.acu.pie.retrofit.model;

import lombok.Data;

import java.util.Map;

/**
 * RequestBody
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 4:43 下午
 */
@Data
public class KafkaHttpRequestBody {
    private String session_id;
    private Map<String, Object> session_param;
    private String recog_result;
    private int recog_type;
    private String begin_time;
    private String end_time;
}
