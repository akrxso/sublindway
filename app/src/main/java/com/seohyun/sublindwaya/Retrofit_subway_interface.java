package com.seohyun.sublindwaya;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Retrofit_subway_interface {
    @GET("subway-name-by-location")
    Call<xy_model> test_api_get(
            @Query("userId") String id,
            @Query("locationX") double locationX,
            @Query("locationY") double locationY
    );

}
