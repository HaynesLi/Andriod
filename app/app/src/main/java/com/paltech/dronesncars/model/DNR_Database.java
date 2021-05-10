package com.paltech.dronesncars.model;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Rover.class, DroneSetting.class, RoverRoutine.class,
        RoverRoute.class, Map.class, Result.class}, version = 1)
public abstract class DNR_Database extends RoomDatabase {

    private static final String DB_NAME = "dnrDatabase.db";
    private static volatile DNR_Database INSTANCE;


    public static DNR_Database getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, DNR_Database.class, DB_NAME).build();
        }
        return INSTANCE;
    }

    public abstract RoverDAO getRoverDAO();

    public abstract DroneSettingDAO getDroneSettingDAO();

    public abstract RoverRouteDAO getRoverRouteDAO();

    public abstract RoverRoutineDAO getRoverRoutineDAO();
}

