package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoverRouteDAO {

    @Insert
    void insert(RoverRoute rover_route);

    @Update
    void update(RoverRoute... rover_routes);

    @Delete
    void delete(RoverRoute... rover_routes);

    @Query("SELECT * FROM RoverRoute")
    List<RoverRoute> getAllRoverRoutes();

    @Query("SELECT * FROM RoverRoute WHERE routine_id = (:routine_id)")
    List<RoverRoute> findRoverRoutesForRoutine(int routine_id);

    @Transaction
    @Query("SELECT * FROM Rover")
    public List<RoverAndRoute> getRoversAndRoutes();


    // TODO does this work like this?
    @Transaction
    @Query("SELECT * FROM Rover, RoverRoute where rover_route_id =(:rover_route_id)")
    public Rover getRoverForRoute(int rover_route_id);
}
