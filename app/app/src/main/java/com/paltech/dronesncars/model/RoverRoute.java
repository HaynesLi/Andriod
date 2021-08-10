package com.paltech.dronesncars.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = RoverRoutine.class, parentColumns = "rover_routine_id",
        childColumns = "routine_id", onDelete = CASCADE))
public class RoverRoute {

    @PrimaryKey
    @ColumnInfo(name = "rover_route_id")
    @NonNull
    public String rover_route_id;
    // TODO id = yyyy-mm-dd_hh-mm

    @ColumnInfo(name = "corresponding_rover_id")
    public int corresponding_rover_id;

    @ColumnInfo(name = "route")
    public List<GeoPoint> route;

    @ColumnInfo(name = "is_navigation_point")
    public List<Boolean> is_navigation_point;

    @ColumnInfo(name = "routine_id")
    public int routine_id;

    public RoverRoute(@NotNull String rover_route_id, int corresponding_rover_id, List<GeoPoint> route, int routine_id) {
        this.rover_route_id = rover_route_id;
        this.corresponding_rover_id = corresponding_rover_id;
        this.route = route;
        this.routine_id = routine_id;
        this.is_navigation_point = new ArrayList<>(Collections.nCopies(route.size(), false));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoverRoute that = (RoverRoute) o;
        return rover_route_id.equals(that.rover_route_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rover_route_id);
    }
}
