package com.paltech.dronesncars.ui;

import com.google.gson.annotations.SerializedName;

import org.osmdroid.util.GeoPoint;

public class RoverUpdateModel {
    @SerializedName("battery")
    private int battery;

    @SerializedName("latitude")
    private Float latitude;

    @SerializedName("longitude")
    private Float longitude;

    @SerializedName("mission_id")
    private String mission_id;

    @SerializedName("last_waypoint")
    private int currentWaypoint;

    public int getBattery(){
        return this.battery;
    }

    public Float getLatitude(){
        return this.latitude;
    }

    public Float getLongitude(){
        return this.longitude;
    }

    public String getMission(){ return this.mission_id; }

    public int getCurrentWaypoint(){
        return this.currentWaypoint;
    }

}
