package com.seohyun.sublindwaya;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit_subway {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://15.164.219.39:8079/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            // Create a custom Gson instance with lenient parsing
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Use the custom Gson instance
                    .build();
        }
        return retrofit;
    }

    // Get the Retrofit_subway_interface service
    public static Retrofit_subway_interface getSubwayService() {
        return getInstance().create(Retrofit_subway_interface.class);
    }

    // 추가된 메서드: Retrofit_send_img 서비스를 제공
    public static Retrofit_send_img getSendImgService() {
        return getInstance().create(Retrofit_send_img.class);
    }
}