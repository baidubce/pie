package com.baidu.acu.pie.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * http工具
 */
@Slf4j
public class HttpUtil {

    /**
     * 同步get请求,返回String
     * @throws: RuntimeException
     */
    public static String syncHttpGetStringResult(String url){
        try {
            return syncHttpGet(url).string();
        } catch (IOException e) {
            throw new RuntimeException("get http result error:" + e.getMessage());
        }
    }

    /**
     * 同步get请求，返回InputStream
     *  @throws: RuntimeException
     */
    public static InputStream syncHttpGetInputStream(String url) {
        return syncHttpGet(url).byteStream();
    }

    /**
     * 同步get请求，返回body结果
     */
    private static ResponseBody syncHttpGet(String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            }else {
                throw new RuntimeException("http get method occur exception:" + response.message());
            }
        } catch (IOException e) {
            throw new RuntimeException("http get method occur exception:" + e.getMessage());
        }
    }
}
