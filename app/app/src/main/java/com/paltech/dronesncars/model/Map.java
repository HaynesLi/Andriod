package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;

@Entity
public class Map {
    @PrimaryKey
    @ColumnInfo(name = "map_id")
    public int map_id;

    @ColumnInfo(name = "polygon")
    public Polygon polygon;

    @ColumnInfo(name = "drone_route")
    public List<GeoPoint> drone_route;

    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

}
