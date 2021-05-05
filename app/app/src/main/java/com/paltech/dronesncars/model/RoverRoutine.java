package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class RoverRoutine {

    @PrimaryKey
    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

    @ColumnInfo(name = "num_of_rovers")
    public int num_of_rovers;

    // TODO add Foreign Key annotation and combined object and so on...
    // TODO add typeconverter for List
    @ColumnInfo(name = "route_ids")
    public List<Integer> route_ids;
}
