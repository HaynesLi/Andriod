package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = RoverRoutine.class, parentColumns = "rover_routine_id",
        childColumns = "routine_id", onDelete = CASCADE))
public class RoverRoute {

    @PrimaryKey
    @ColumnInfo(name = "rover_route_id")
    public int rover_route_id;

    @ColumnInfo(name = "rover_id")
    public int rover_id;

    @ColumnInfo(name = "route")
    public List<GeoPoint> route;

    @ColumnInfo(name = "routine_id")
    public int routine_id;

}
