package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

@Entity
public class RoverRoutine {

    @PrimaryKey
    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

    @ColumnInfo(name = "num_of_rovers")
    public int num_of_rovers;

    @Ignore
    public RoverRoutine(int rover_routine_id, int num_of_rovers) {
        this.rover_routine_id = rover_routine_id;
        this.num_of_rovers = num_of_rovers;
    }

    public RoverRoutine(int rover_routine_id) {
        this.rover_routine_id = rover_routine_id;
        this.num_of_rovers = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoverRoutine that = (RoverRoutine) o;
        return rover_routine_id == that.rover_routine_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rover_routine_id);
    }
}
