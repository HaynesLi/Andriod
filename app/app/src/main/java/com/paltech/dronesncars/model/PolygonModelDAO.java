package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PolygonModelDAO {
    @Query("SELECT * FROM PolygonModel WHERE polygon_id = (:polygon_id)")
    PolygonModel getPolygonModelByID(int polygon_id);

    @Insert
    void insertPolygonModel(PolygonModel polygonModel);

    @Update
    void updatePolygonModel(PolygonModel polygonModel);

    @Delete
    void deletePolygonModel(PolygonModel polygonModel);
}
