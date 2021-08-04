package com.paltech.dronesncars.model;

import org.osmdroid.util.GeoPoint;

import java.util.Objects;

public class Waypoint {

    public String corresponding_route_id;

    public int waypoint_number;

    public boolean is_navigation_point;

    public GeoPoint position;

    public boolean milestone_completed;

    public String mission_id;

    public Waypoint(String corresponding_route_id, int waypoint_number, GeoPoint position, boolean is_navigation_point, String mission_id) {
        this.corresponding_route_id = corresponding_route_id;
        this.waypoint_number = waypoint_number;
        this.position = position;
        this.is_navigation_point = is_navigation_point;
        this.mission_id = mission_id;
        this.milestone_completed = false;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waypoint waypoint = (Waypoint) o;
        return corresponding_route_id == waypoint.corresponding_route_id && waypoint_number == waypoint.waypoint_number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(corresponding_route_id+":"+waypoint_number);
    }
}
