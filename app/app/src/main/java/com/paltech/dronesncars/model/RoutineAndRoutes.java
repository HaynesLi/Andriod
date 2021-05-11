package com.paltech.dronesncars.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class RoutineAndRoutes {
    @Embedded public RoverRoutine routine;

    @Relation(
            parentColumn = "rover_routine_id",
            entityColumn = "routine_id"
    )
    public List<RoverRoute> roverRoutes;

}
