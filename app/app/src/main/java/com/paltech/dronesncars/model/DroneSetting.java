package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class DroneSetting {
    @PrimaryKey
    @ColumnInfo(name = "settings_id")
    public int settings_id; // TODO: find something more reasonable (e.g. timestamp?)

    @ColumnInfo(name = "flight_altitude")
    public int flight_altitude; // TODO: find & add additional reasonable parameters (e.g. overlap?)

    public DroneSetting(int settings_id, int flight_altitude) {
        this.settings_id = settings_id;
        this.flight_altitude = flight_altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DroneSetting that = (DroneSetting) o;
        return settings_id == that.settings_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(settings_id);
    }
}
