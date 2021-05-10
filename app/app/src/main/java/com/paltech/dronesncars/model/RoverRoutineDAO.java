package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoverRoutineDAO {

    @Insert
    void insert (RoverRoutine rover_routine);

    @Update
    void update(RoverRoutine... rover_routines);

    @Delete
    void delete(RoverRoutine... rover_routines);

    @Query("SELECT * FROM RoverRoutine")
    List<RoverRoutine> getAllRoverRoutines();

    @Query("SELECT * FROM RoverRoutine WHERE rover_routine_id = (:rover_routine_id)")
    RoverRoutine getRoverRoutineByID(int rover_routine_id);

}
