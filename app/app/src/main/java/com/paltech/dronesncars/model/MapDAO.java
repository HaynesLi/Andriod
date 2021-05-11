package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MapDAO {

    @Insert
    public void insertMap(Map map);

    @Update
    public void updateMap(Map map);

    @Delete
    public void deleteMap(Map map);

    @Query("SELECT * FROM map")
    public List<Map> getAllMaps();

    @Query("SELECT * FROM map WHERE map_id = (:map_id)")
    public Map getMapFromId(int map_id);
}
