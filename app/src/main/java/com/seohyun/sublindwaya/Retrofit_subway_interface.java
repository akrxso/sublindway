package com.seohyun.sublindwaya;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Retrofit_subway_interface {
    @GET("{x}/{y}")
    Call<xy_model> test_api_get(
            @Path("x") Double x,
            @Path("y") Double y);
}
