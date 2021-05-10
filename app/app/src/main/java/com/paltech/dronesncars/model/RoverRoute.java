package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = RoverRoute.class, parentColumns = "rover_routine_id",
        childColumns = "routine_id", onDelete = CASCADE))
public class RoverRoute {

    @PrimaryKey
    @ColumnInfo(name = "rover_route_id")
    public int rover_route_id;

    // TODO: add ForeignKey annotation and combined object and so on...
    @ColumnInfo(name = "rover_route_id")
    public int rover_id;

    private class GeoPoint {
        // TODO: this is just here to prevent syntax erros, as long as I haven't imported osmdroid
    }

    // TODO: add typeconverter for list and GeoPoint
    @ColumnInfo(name = "route")
    public List<GeoPoint> route;

    @ColumnInfo(name = "routine_id")
    public int routine_id;

}
