package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Rover {
    @PrimaryKey
    @ColumnInfo(name = "rover_id")
    public int rover_id;

    @ColumnInfo(name = "rover_name")
    public String roverName;

    @ColumnInfo(name = "battery")
    public double battery;
}
