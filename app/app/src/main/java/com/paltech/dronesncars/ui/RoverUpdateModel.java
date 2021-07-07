package com.paltech.dronesncars.ui;

import com.google.gson.annotations.SerializedName;

public class RoverUpdateModel {
    @SerializedName("battery")
    private int battery;

    @SerializedName("position")
    private String position;

    @SerializedName("mission")
    private String mission;

    @SerializedName("currentWaypoint")
    private int currentWaypoint;

    public int getBattery(){
        return this.battery;
    }

    public String getPosition(){
        return this.position;
    }

    public String getMission(){
        return this.mission;
    }

    public int getCurrentWaypoint(){
        return this.currentWaypoint;
    }

}
