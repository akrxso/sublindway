package com.seohyun.sublindwaya;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class nextStationName_model {
    //역 구독
    @SerializedName("next-station-name")
    @Expose
    private String nextStationName;

    public String getnextStationName(){
        return nextStationName;
    }


}
