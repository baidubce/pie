package com.pie.demo.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("/text2audio")
    Call<ResponseBody> text2audio(@Query("tex") String tex,
                                  @Query("lan") String lan,
                                  @Query("pdt") int pdt,
                                  @Query("ctp") int ctp,
                                  @Query("cuid") String cuid,
                                  @Query("spd") int spd,
                                  @Query("pit") int pit,
                                  @Query("vol") int vol);
}
