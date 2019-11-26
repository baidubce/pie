package com.baidu.acu.pie.retrofit;

import com.baidu.acu.pie.retrofit.model.KafkaHttpRequestBody;
import com.baidu.acu.pie.retrofit.model.KafkaHttpResultResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Map;

/**
 * KafkaHttpService
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 3:36 下午
 */
public interface KafkaHttpService {
    @POST("/asr/api/data/transmission")
    Call<KafkaHttpResultResponse<Map<String, Object>>> sendRequest(@Body KafkaHttpRequestBody req);
}
