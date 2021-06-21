package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;

import java.util.Objects;

@Entity
public class Result {

    @PrimaryKey
    @ColumnInfo(name = "result_id")
    public int result_id;

    @ColumnInfo(name = "certainty")
    public double certainty;

    @ColumnInfo(name = "location")
    public GeoPoint location;

    @Ignore
    public Result(int result_id, double certainty) {
        this.result_id = result_id;
        this.certainty = certainty;
    }

    public Result(int result_id, double certainty, GeoPoint location) {
        this.result_id = result_id;
        this.certainty = certainty;
        this.location = location;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return result_id == result.result_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(result_id);
    }
}
