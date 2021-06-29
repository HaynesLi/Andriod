package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.net.InetAddress;
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

    @Update
    void update(Rover rover);

    @Query("DELETE FROM rover")
    void deleteAllRovers();

    @Query("SELECT Count(rover_id) FROM rover WHERE is_used")
    int get_num_of_used_rovers();

    @Query("SELECT Count(rover_id) FROM rover WHERE is_used")
    LiveData<Integer> get_num_of_used_rovers_livedata();

    // TODO: add Transaction to get all routes for one rover
    @Transaction
    @Query("SELECT * FROM RoverRoute, Rover WHERE rover_id = (:rover_id) and corresponding_rover_id = rover_id")
    List<RoverRoute> getRoutesForRover(int rover_id);

    @Query("SELECT rover_id FROM rover ORDER BY rover_id")
    LiveData<List<Integer>> get_all_ids();

    @Query("SELECT rover_id FROM rover ORDER BY rover_id")
    List<Integer> get_all_ids_not_livedata();

    @Query("SELECT ip_address FROM rover ORDER BY rover_id")
    LiveData<List<InetAddress>> get_all_ip_addresses();

    @Query("SELECT ip_address FROM rover ORDER BY rover_id")
    List<InetAddress> get_all_ip_addresses_not_livedata();
}
