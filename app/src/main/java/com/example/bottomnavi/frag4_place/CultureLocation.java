package com.example.bottomnavi.frag4_place;

public class CultureLocation {
    private double latitude;
    private double longitude;
    private String name;

    public CultureLocation(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}
