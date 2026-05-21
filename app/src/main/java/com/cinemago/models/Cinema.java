package com.cinemago.models;

public class Cinema {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private float rating;
    private String placeId;

    public Cinema(String name, String address, double latitude, double longitude,
                  float rating, String placeId) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.placeId = placeId;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRating() { return rating; }
    public String getPlaceId() { return placeId; }
}