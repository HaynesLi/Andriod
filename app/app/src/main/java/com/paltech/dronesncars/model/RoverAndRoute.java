package com.paltech.dronesncars.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * The table we use to associate rovers with routes
 */
public class RoverAndRoute {
    @Embedded public Rover rover;
    @Relation(
            parentColumn = "rover_id",
            entityColumn = "corresponding_rover_id"
    )
    public List<RoverRoute> routes;
}
