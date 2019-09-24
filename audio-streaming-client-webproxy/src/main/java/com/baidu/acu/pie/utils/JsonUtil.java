package com.baidu.acu.pie.utils;


import com.baidu.acu.pie.exception.WebProxyException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JsonUtil {

    public static ObjectMapper objectMapper = createObjectMapper();

    /**
     * 从json 字符串中解析数据
     * @throws WebProxyException: json解析失败，或者没有key时抛出异常
     */
    public static String parseJson(String json, String key) {
        JsonNode value = parseJsonNode(json, key);
        if (value == null) {
            throw new WebProxyException("key:" + key + "is not in json " + json);
        }

        // 如果当前node下面只有一个value(没有node结构)，则使用asText()方法；若使用toString()则返回字符串增加了多余的""
        if (value.isValueNode()) {
            return value.asText();
        }
        return value.toString();
    }

    /**
     * 从json中检测key是否存在，true为存在
     */
    public static boolean keyExist(String json, String key) {
        JsonNode value = parseJsonNode(json, key);
        return value != null;
    }

    /**
     * 将对象转化为string
     */
    public static String transObjectToStr(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new WebProxyException("transform object to string exception:" + e.getMessage());
        }
    }

    private static JsonNode parseJsonNode(String json, String key) {
        JsonNode node;
        try {
            node = objectMapper.readTree(json);
        } catch (IOException e) {
            String errorMsg = "parse json :" + json + " occur exception " + e.getMessage();
            log.info(errorMsg);
            throw new WebProxyException(errorMsg);
        }
        return node.get(key);
    }

    private static ObjectMapper createObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());

        return objectMapper;
    }


}
