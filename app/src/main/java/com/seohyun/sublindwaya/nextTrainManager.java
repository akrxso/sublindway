package com.seohyun.sublindwaya;

public class nextTrainManager {
    private static nextTrainManager instance;
    private static String currentStation;
    private String subwayLine;

    // Private constructor to prevent instantiation
    private nextTrainManager() {}

    // Synchronized method to control simultaneous access
    public static synchronized nextTrainManager getInstance() {
        if (instance == null) {
            instance = new nextTrainManager();
        }
        return instance;
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(String currentStation) {
        this.currentStation = currentStation;
    }

    public String getSubwayLine() {
        return subwayLine;
    }

    public void setSubwayLine(String subwayLine) {
        this.subwayLine = subwayLine;
    }
}