package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.List;
import java.util.Objects;

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

    public Map(int map_id, Polygon polygon, List<GeoPoint> drone_route, int rover_routine_id) {
        this.map_id = map_id;
        this.polygon = polygon;
        this.drone_route = drone_route;
        this.rover_routine_id = rover_routine_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Map map = (Map) o;
        return map_id == map.map_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(map_id);
    }
}
