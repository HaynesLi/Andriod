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
}
