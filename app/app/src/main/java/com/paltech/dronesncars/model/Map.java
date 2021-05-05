package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Map {
    @PrimaryKey
    @ColumnInfo(name = "map_id")
    public int map_id;

    @ColumnInfo(name = "polygon")
    // TODO: add a type converter! --> depends on the actual implementation of osmdroid.Polygon...
    //  maybe to json?
    public Polygon polygon;

    // TODO add typeconverter for List
    @ColumnInfo(name = "drone_route")
    public List<GeoPoint> drone_route;

    // TODO: add foreign key entity plus combined object and so on...
    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

    private class Polygon {
        // TODO: this  is just here to prevent java errors,
        //  as long as I haven't imported the osmdroid-library
    }

    private class GeoPoint {
        // TODO: this  is just here to prevent java errors,
        //  as long as I haven't imported the osmdroid-library
    }
}
