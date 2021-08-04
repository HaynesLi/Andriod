package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class RoverRoutine {

    @PrimaryKey
    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

    @ColumnInfo(name = "num_of_rovers")
    public int num_of_rovers;

    @ColumnInfo(name = "rover_route_ids")
    public List<String> rover_route_ids;

    @Ignore
    public RoverRoutine(int rover_routine_id, List<String> rover_routes_ids, int num_of_rovers) {
        this.rover_routine_id = rover_routine_id;
        this.rover_route_ids = rover_route_ids;
        this.num_of_rovers = num_of_rovers;
    }

    public RoverRoutine(int rover_routine_id) {
        this.rover_routine_id = rover_routine_id;
        this.rover_route_ids = new ArrayList<>();
        this.num_of_rovers = 0;
    }

    @Ignore
    public RoverRoutine(int rover_routine_id, int num_of_rovers) {
        this.rover_routine_id = rover_routine_id;
        this.rover_route_ids = new ArrayList<>();
        this.num_of_rovers = num_of_rovers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoverRoutine that = (RoverRoutine) o;
        boolean id_correct = rover_routine_id == that.rover_routine_id;
        boolean routes_correct = rover_route_ids.equals(((RoverRoutine) o).rover_route_ids);
        return id_correct && routes_correct;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rover_routine_id);
    }
}
