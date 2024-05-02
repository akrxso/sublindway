package com.seohyun.sublindwaya;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Retrofit_send_img {
    String BASE_URL = "http://13.209.19.20:8079/"; // 자신의 서버 IP 또는 도메인

    @Multipart
    @POST("send_img") // 파일 경로
    Call<String> test_send_img(
            @Part MultipartBody.Part file
    );
}
