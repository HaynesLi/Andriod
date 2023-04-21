package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * The Dao for {@link PolygonModel}
 */
@Dao
public interface PolygonModelDAO {
    @Query("SELECT * FROM PolygonModel WHERE polygon_id = (:polygon_id)")
    PolygonModel getPolygonModelByID(int polygon_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPolygonModel(PolygonModel polygonModel);

    @Update
    void updatePolygonModel(PolygonModel polygonModel);

    @Delete
    void deletePolygonModel(PolygonModel polygonModel);
}
