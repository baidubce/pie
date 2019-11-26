package com.baidu.acu.pie.retrofit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ResultResponse
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 3:37 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaHttpResultResponse<T> {
    private int retCode;
    private String errMsg;
    private Map<String, Object> result;
}
