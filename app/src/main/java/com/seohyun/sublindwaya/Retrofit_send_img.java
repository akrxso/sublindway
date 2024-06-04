package com.seohyun.sublindwaya;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Retrofit_send_img {
    String BASE_URL = "http://15.164.219.39:8079/";
    @Multipart
    @POST("/send-subways-image")
    Call<ServerResponse> test_send_img(
            @Part MultipartBody.Part file,
            @Query("kakaoId") String kakaoId,
            @Query("locationX") double locationX,
            @Query("locationY") double locationY
    );
}