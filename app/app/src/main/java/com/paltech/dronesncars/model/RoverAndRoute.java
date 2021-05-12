package com.paltech.dronesncars.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RoverAndRoute {
    @Embedded public Rover rover;
    @Relation(
            parentColumn = "rover_id",
            entityColumn = "corresponding_rover_id"
    )
    public List<RoverRoute> routes;
}
