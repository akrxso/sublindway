package com.seohyun.sublindwaya;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit_subway {

    private static final String BASE_URL = "http://13.209.19.20:8079/";


    public static Retrofit_subway_interface getApiService(){return getInstance().create(Retrofit_subway_interface.class);}

    private static Retrofit getInstance(){
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
