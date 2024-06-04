package com.seohyun.sublindwaya;

public class trainLocationManager {
    private static trainLocationManager instance;
    private static String trainLocation;
    private String direction;  // 상행/하행 정보를 저장할 변수

    private trainLocationManager() {}  // Private constructor to prevent instantiation

    public static synchronized trainLocationManager getInstance() {
        if (instance == null) {
            instance = new trainLocationManager();
        }
        return instance;
    }
    public static String gettrainLocation() {
        return trainLocation;
    }

    public void settrainLocation(String trainLocation) {
        this.trainLocation = trainLocation;
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

}



