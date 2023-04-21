package com.paltech.dronesncars.model;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import androidx.room.TypeConverters;

/**
 * The Room database used to store different info.
 * For a Room Database you need:
 * 1. Entities: 1 Entity specifies 1 table in the sql database,
 *  - annotated with @Entity
 * 2. DAOs (Data Access Objects): A DAO specifies the queries one can do on the different tables,
 *  - annotated with @Dao
 * 3. This database class which groups all the info together and triggers the code generation,
 *  - annotated with @Database(entities = list of your entities in curled braces), version = ...)
 *  - annotated with @TypeConverters(your typeconverter class in curled braces), which is used to
 *    convert complex objects into datatypes that can be saved in a sql database, e.g. into
 *    json strings
 */
@androidx.room.Database(entities = {Rover.class, DroneSetting.class, RoverRoutine.class,
        RoverRoute.class, FlightRoute.class, Result.class, PolygonModel.class}, version = 1)
@TypeConverters({com.paltech.dronesncars.model.TypeConverters.class})
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

    public abstract ResultDAO getResultDAO();

    public abstract PolygonModelDAO getPolygonModelDAO();

    public abstract FlightRouteDAO getFlightRouteDAO();
}

