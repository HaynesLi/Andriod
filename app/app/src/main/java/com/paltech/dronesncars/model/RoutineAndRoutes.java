package com.paltech.dronesncars.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * The class we use to add a table that maps routes to routines.
 * TODO never used? should we delete it than?
 */
public class RoutineAndRoutes {
    @Embedded public RoverRoutine routine;

    @Relation(
            parentColumn = "rover_routine_id",
            entityColumn = "routine_id"
    )
    public List<RoverRoute> roverRoutes;

}
