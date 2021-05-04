package com.paltech.dronesncars;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Rover {
    @PrimaryKey
    public int rid;

    @ColumnInfo(name = "rover_name")
    public String roverName;
}
