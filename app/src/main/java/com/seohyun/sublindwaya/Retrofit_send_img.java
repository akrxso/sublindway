package com.seohyun.sublindwaya;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Retrofit_send_img {
    String BASE_URL = "http://15.164.219.39:8079/"; // 자신의 서버 IP 또는 도메인

    @Multipart
    @POST("send-subways-image") // 수정된 파일 경로
//    Call<String> test_send_img(
//            @Part MultipartBody.Part file
//    );
        Call<String> test_send_img(
            @Part MultipartBody.Part file,
            @Part("kakaoId") RequestBody kakaoId,
            @Part("locationX") RequestBody locationX,
            @Part("locationY") RequestBody locationY
    );
}
