package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.Objects;

/**
 * The table used to save the one drone flight route. Similar to {@link DroneSetting} we currently only save one
 * route instead of storing multiple and selecting the newest one.
 */
@Entity
public class FlightRoute {
    @PrimaryKey
    @ColumnInfo(name = "flight_route_id")
    public int flight_route_id;

    @ColumnInfo(name = "route")
    public List<GeoPoint> route;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightRoute that = (FlightRoute) o;
        return flight_route_id == that.flight_route_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flight_route_id);
    }
}
