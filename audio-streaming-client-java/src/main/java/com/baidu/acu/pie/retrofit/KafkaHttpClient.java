package com.baidu.acu.pie.retrofit;

import com.baidu.acu.pie.retrofit.model.KafkaHttpRequestBody;
import com.baidu.acu.pie.retrofit.model.KafkaHttpResultResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * KafkaHttpClient
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 3:47 下午
 */
@Slf4j
public class KafkaHttpClient {
    private KafkaHttpService service;
    public KafkaHttpClient(String server, int port) {
        RetrofitServiceFactory retrofitServiceFactory = new RetrofitServiceFactory();
        service = retrofitServiceFactory.createService(server, port, KafkaHttpService.class);
    }

    public KafkaHttpResultResponse<Map<String, Object>> sendRequest(KafkaHttpRequestBody req) {
        try {
            return ResponseBodyHandler.getBody(service.sendRequest(req).execute());
        } catch (IOException e) {
            log.error("send request to kafka http encounter an error!");
            return new KafkaHttpResultResponse<Map<String, Object>>(
                    -1,
                    "send request to kafka http error",
                    new HashMap<String, Object>());
        }
    }
}
