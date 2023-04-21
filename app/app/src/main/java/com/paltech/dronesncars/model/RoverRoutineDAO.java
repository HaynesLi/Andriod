package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * The Dao for {@link RoverRoutine}
 */
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

    @Query("SELECT * FROM RoverRoutine WHERE rover_routine_id = (:rover_routine_id)")
    LiveData<RoverRoutine> get_rover_routine_by_id_livedata(int rover_routine_id);

}
