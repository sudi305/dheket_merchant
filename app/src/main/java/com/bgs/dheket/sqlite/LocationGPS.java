package com.bgs.dheket.sqlite;

/**
 * Created by SND on 7/2/2016.
 */
public class LocationGPS {
    double latitude;
    double longitude;

    public LocationGPS(){

    }

    public LocationGPS(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
