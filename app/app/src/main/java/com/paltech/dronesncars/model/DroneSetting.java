package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DroneSetting {
    @PrimaryKey
    @ColumnInfo(name = "settings_id")
    public int settings_id; // TODO: find something more reasonable (e.g. timestamp?)

    @ColumnInfo(name = "flight_altitude")
    public int flight_altitude; // TODO: find & add additional reasonable parameters (e.g. overlap?)
}
