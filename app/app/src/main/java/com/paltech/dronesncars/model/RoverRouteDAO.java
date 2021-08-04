package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
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
    void insertMultiple(RoverRoute... rover_route);

    @Update
    void update(RoverRoute... rover_routes);

    @Delete
    void delete(RoverRoute... rover_routes);

    @Query("SELECT * FROM RoverRoute")
    List<RoverRoute> getAllRoverRoutes();

    @Query("SELECT * FROM RoverRoute")
    LiveData<List<RoverRoute>> get_all_rover_routes_livedata();

    @Query("SELECT * FROM RoverRoute WHERE routine_id = (:routine_id)")
    List<RoverRoute> findRoverRoutesForRoutine(int routine_id);

    @Query("SELECT * FROM RoverRoute WHERE rover_route_id = (:rover_route_id)")
    RoverRoute get_rover_route_by_id(String rover_route_id);

    @Query("SELECT * FROM RoverRoute WHERE rover_route_id = (:rover_route_id)")
    LiveData<RoverRoute> get_rover_route_by_id_livedata(String rover_route_id);

    @Transaction
    @Query("SELECT * FROM Rover")
    List<RoverAndRoute> getRoversAndRoutes();


    // TODO does this work like this?
    @Transaction
    @Query("SELECT * FROM Rover, RoverRoute where rover_id = corresponding_rover_id AND rover_route_id = (:rover_route_id)")
    Rover getRoverForRoute(String rover_route_id);

    @Query("DELETE FROM RoverRoute WHERE routine_id == (:routine_id)")
    void delete_rover_routes_by_routine_id(int routine_id);
}
