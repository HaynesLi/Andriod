package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface RoverDAO {
    @Query("SELECT * FROM rover")
    List<Rover> getAll();

    @Query("SELECT * FROM rover")
    LiveData<List<Rover>> getAllLiveData();

    @Query("SELECT * FROM rover WHERE rover_id = (:rid)")
    Rover getRoverByID(int rid);

    @Insert
    void insertMultipleRovers(Rover... rovers);

    @Delete
    void delete(Rover rover);

    @Query("DELETE FROM rover")
    void deleteAllRovers();

    // TODO: add Transaction to get all routes for one rover
    @Transaction
    @Query("SELECT * FROM RoverRoute, Rover WHERE rover_id = (:rover_id) and corresponding_rover_id = rover_id")
    List<RoverRoute> getRoutesForRover(int rover_id);
}
