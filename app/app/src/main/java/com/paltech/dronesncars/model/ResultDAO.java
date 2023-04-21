package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * The Dao for {@link Result}
 */
@Dao
public interface ResultDAO {

    @Insert
    void insertMultipleResults(Result... result);

    @Update
    void updateResult(Result result);

    @Delete
    void deleteResults(Result... result);

    @Query("DELETE FROM result")
    void delete_all_results();

    @Query("SELECT * FROM result")
    List<Result> getAllResults();

    @Query("SELECT * FROM result")
    LiveData<List<Result>> get_all_results_livedata();

}