package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

@Entity
public class Rover {

    @PrimaryKey
    @ColumnInfo(name = "rover_id")
    public int rover_id;

    @ColumnInfo(name = "ip_address")
    public InetAddress ip_address;

    @ColumnInfo(name = "latitude")
    public Float latitude;

    @ColumnInfo(name = "longitude")
    public Float longitude;

    @ColumnInfo(name = "position")
    public GeoPoint position;

    @ColumnInfo(name = "rover_name")
    public String roverName;

    @ColumnInfo(name = "battery")
    public int battery;

    @ColumnInfo(name = "status")
    public RoverStatus status;

    @ColumnInfo(name = "mission")
    public int mission;

    @ColumnInfo(name = "waypoints")
    public List<Waypoint> waypoints;

    @ColumnInfo(name = "currentWaypoint")
    public int currentWaypoint;

    @ColumnInfo(name = "progress")
    public double progress;

    @ColumnInfo(name = "is_used", defaultValue = "False")
    public boolean is_used;

    public Rover(int rover_id, InetAddress ip_address) {
        this.rover_id = rover_id;
        this.ip_address = ip_address;
        this.status = RoverStatus.DISCONNECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rover rover = (Rover) o;
        return rover_id == rover.rover_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rover_id);
    }
}
