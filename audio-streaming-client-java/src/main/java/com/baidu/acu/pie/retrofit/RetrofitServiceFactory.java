package com.baidu.acu.pie.retrofit;

import com.baidu.acu.pie.util.JacksonUtil;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * RetrofitServiceFactory
 *
 * @Author Xia Shuai(xiashuai01@baidu.com)
 * @Create 2019/11/25 3:49 下午
 */
public class RetrofitServiceFactory {

    private Retrofit createRetrofit(HttpUrl url) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(JacksonUtil.getObjectMapper()))
                .client(httpClient.build())
                .build();
    }

    public <T> T createService(String serverHost, int serverPort, Class<T> service) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host(serverHost)
                .port(serverPort)
                .build();

        Retrofit retrofit = createRetrofit(url);

        return retrofit.create(service);
    }

}