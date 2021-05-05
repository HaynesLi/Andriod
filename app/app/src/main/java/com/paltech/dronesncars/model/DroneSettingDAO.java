package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface DroneSettingDAO {

    @Query("SELECT * FROM dronesetting WHERE settings_id = (:settings_id)")
    DroneSetting getDroneSettingByID(int settings_id);

    @Insert
    void insertSetting(DroneSetting setting);

    @Delete
    void delete(DroneSetting setting);

    @Update
    void updateSetting(DroneSetting setting);

    // TODO: does it work like this (return exactly one value,
    //  even though in theory there could be multiple?)
    @Query("SELECT flight_altitude FROM dronesetting where settings_id = (:settings_id)")
    int getFlightAltitude(int settings_id);
}
