package com.seohyun.sublindwaya;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class kakaoLogin extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "b3116e93a5aacdd153057e7d197c2210");
    }

}
