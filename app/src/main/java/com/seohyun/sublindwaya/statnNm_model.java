package com.seohyun.sublindwaya;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class statnNm_model {
    //역 구독
    @SerializedName("statnNm")
    @Expose
    private String subwayName;

    public String getSubwayName(){
        return subwayName;
    }

    @SerializedName("subwayNm")
    @Expose
    private String subwayLineName;

    public String getSubwayLineName(){
        return subwayLineName;
    }


}