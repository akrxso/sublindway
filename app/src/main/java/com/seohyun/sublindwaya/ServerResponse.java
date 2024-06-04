package com.seohyun.sublindwaya;

public class ServerResponse {
    private static ServerResponse instance;
    private String trainNum;
    private String upDown;
    private double locationX;
    private double locationY;

    // Private constructor to prevent instantiation
    private ServerResponse() {}

    // Synchronized method to control simultaneous access
    public static synchronized ServerResponse getInstance() {
        if (instance == null) {
            instance = new ServerResponse();
        }
        return instance;
    }

    // Getters and Setters
    public String getTrainNum() {
        return trainNum;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }

    public String getUpDown() {
        return upDown;
    }

    public void setUpDown(String upDown) {
        this.upDown = upDown;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }
}