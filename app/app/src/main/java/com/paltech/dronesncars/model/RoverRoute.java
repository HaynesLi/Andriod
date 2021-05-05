package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
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


}
