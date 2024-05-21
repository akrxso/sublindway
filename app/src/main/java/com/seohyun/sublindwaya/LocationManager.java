package com.seohyun.sublindwaya;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class LocationManager {
    private static LocationManager instance;
    private final MutableLiveData<String> locationLiveData = new MutableLiveData<>();
    private double latitude;
    private double longitude;

    private LocationManager() {
    }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public LiveData<String> getLocationLiveData() {
        return locationLiveData;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        // Format the location into a string and post it
        String locationString = "Latitude: " + latitude + ", Longitude: " + longitude;
        locationLiveData.postValue(locationString);  // Update LiveData with the new location
    }
}
