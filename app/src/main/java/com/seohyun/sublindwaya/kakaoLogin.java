package com.seohyun.sublindwaya;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class kakaoLogin extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "47dc23bd540343a458fdd1d1ac570e31");
    }

}
