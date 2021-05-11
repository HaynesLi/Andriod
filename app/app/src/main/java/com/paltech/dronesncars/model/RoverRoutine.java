package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class RoverRoutine {

    @PrimaryKey
    @ColumnInfo(name = "rover_routine_id")
    public int rover_routine_id;

    @ColumnInfo(name = "num_of_rovers")
    public int num_of_rovers;

    public RoverRoutine(int rover_routine_id, int num_of_rovers) {
        this.rover_routine_id = rover_routine_id;
        this.num_of_rovers = num_of_rovers;
    }
}
