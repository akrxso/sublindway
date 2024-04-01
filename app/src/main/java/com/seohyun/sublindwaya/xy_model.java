package com.seohyun.sublindwaya;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class xy_model {

    @SerializedName("getStation")
    @Expose
    private String getStation;

    public String getStation(){
        return getStation;
    }

}
