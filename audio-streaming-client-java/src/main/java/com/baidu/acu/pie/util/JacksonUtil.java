package com.baidu.acu.pie.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * JacksonUtil
 *
 * @author  Xia Shuai(xiashuai01@baidu.com)
 */
@Slf4j
public class JacksonUtil {
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 设置jackson的参数
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    public static String objectToString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object {} transfer to jason failed!", object);
        }
        return "";
    }

    /**
     * String转化为javaBean
     *
     * @param jsonStr   json字符串
     * @param valueType 反序列化类名称
     * @param <T> 传入类型
     * @return 序列化结果
     * @throws IOException 序列化失败
     **/
    public static <T> T readValue(String jsonStr, Class<T> valueType) throws IOException {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, valueType);
        } catch (IOException e) {
            log.error("convert from json object error", e);
            throw e;
        }
    }
}

