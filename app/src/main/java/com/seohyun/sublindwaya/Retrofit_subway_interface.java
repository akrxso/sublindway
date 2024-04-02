package com.seohyun.sublindwaya;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Retrofit_subway_interface {
    @GET("get-subway-name")
    Call<xy_model> test_api_get(
            @Query("locationX") double locationX,
            @Query("locationY") double locationY
    );
}
