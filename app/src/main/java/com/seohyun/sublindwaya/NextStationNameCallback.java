package com.seohyun.sublindwaya;

public interface NextStationNameCallback {
    void onSuccess(String nextStationName);
    void onFailure(String errorMessage);
}