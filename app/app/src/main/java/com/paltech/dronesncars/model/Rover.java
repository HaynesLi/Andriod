package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Rover {
    @PrimaryKey
    @ColumnInfo(name = "rover_id")
    public int rover_id;

    @ColumnInfo(name = "rover_name")
    public String roverName;

    @ColumnInfo(name = "battery")
    public double battery;

    @ColumnInfo(name = "Status")
    public RoverStatus status;

    public Rover(int rover_id, String roverName, double battery) {
        this.rover_id = rover_id;
        this.roverName = roverName;
        this.battery = battery;
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
