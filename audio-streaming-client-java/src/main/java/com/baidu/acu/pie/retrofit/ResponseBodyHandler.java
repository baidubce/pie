package com.baidu.acu.pie.retrofit;

import retrofit2.Response;

import java.io.IOException;

/**
 * ResponseBodyHandler
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 3:58 下午
 */
public class ResponseBodyHandler {
    public static <T> T getBody(Response<T> response) throws IOException {
        if (response.isSuccessful()) {
            return response.body();
        } else {
//            throw new RuntimeException(response.errorBody().string());
            throw new IOException("connect to kafka http server error");
        }
    }
}
