package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Result {

    @PrimaryKey
    @ColumnInfo(name = "result_id")
    public int result_id;

    @ColumnInfo(name = "certainty")
    public double certainty;

}
