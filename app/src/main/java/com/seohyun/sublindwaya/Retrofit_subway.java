package com.seohyun.sublindwaya;

import static java.util.Currency.getInstance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit_subway {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://13.209.19.20:8079/";
    public static Retrofit_subway_interface getApiService(){return getInstance().create(Retrofit_subway_interface.class);}
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

