package com.paltech.dronesncars.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ResultDAO {

    @Insert
    public void insertMultipleResults(Result... result);

    @Update
    public void updateResult(Result result);

    @Delete
    public void deleteResults(Result... result);

    @Query("SELECT * FROM result")
    public List<Result> getAllResults();

}