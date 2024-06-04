package com.seohyun.sublindwaya;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class xy_model {
    //역이름 받아오기
    @SerializedName("subwayName")
    @Expose
    private String subwayName;

    public String getSubwayName(){
        return subwayName;
    }


}