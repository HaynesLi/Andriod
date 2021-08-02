package com.paltech.dronesncars.model;

import org.osmdroid.util.GeoPoint;

import java.net.InetAddress;
import java.util.Objects;

public class Waypoint {

    public int corresponding_route_id;

    public int waypoint_number;

    public boolean is_navigation_point;

    public GeoPoint position;

    public boolean milestone_completed;

    public Waypoint(int corresponding_route_id, int waypoint_number, GeoPoint position, boolean is_navigation_point) {
        this.corresponding_route_id = corresponding_route_id;
        this.waypoint_number = waypoint_number;
        this.position = position;
        this.is_navigation_point = is_navigation_point;
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