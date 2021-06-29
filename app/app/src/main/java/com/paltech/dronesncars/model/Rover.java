package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.net.InetAddress;
import java.util.Objects;

@Entity
public class Rover {

    @PrimaryKey
    @ColumnInfo(name = "rover_id")
    public int rover_id;

    @ColumnInfo(name = "ip_address")
    public InetAddress ip_address;

    @ColumnInfo(name = "last_known_position")
    public GeoPoint last_know_position;

    @ColumnInfo(name = "rover_name")
    public String roverName;

    @ColumnInfo(name = "battery")
    public double battery;

    @ColumnInfo(name = "status")
    public RoverStatus status;

    @ColumnInfo(name = "progress")
    public double progress;

    @ColumnInfo(name = "last_reached_target")
    public GeoPoint last_reached_target;

    @ColumnInfo(name = "is_used", defaultValue = "False")
    public boolean is_used;

    public Rover(int rover_id) {
        this.rover_id = rover_id;
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
