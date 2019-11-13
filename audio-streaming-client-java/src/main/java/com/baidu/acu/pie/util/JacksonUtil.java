package com.baidu.acu.pie.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JacksonUtil
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/13 4:10 下午
 */
@Slf4j
public class JacksonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 设置jackson的参数
    }

    public static String objectToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object {} transfer to jason failed!", object);
        }
        return "";
    }
}

