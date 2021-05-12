package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Result {

    @PrimaryKey
    @ColumnInfo(name = "result_id")
    public int result_id;

    @ColumnInfo(name = "certainty")
    public double certainty;

    public Result(int result_id, double certainty) {
        this.result_id = result_id;
        this.certainty = certainty;
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
