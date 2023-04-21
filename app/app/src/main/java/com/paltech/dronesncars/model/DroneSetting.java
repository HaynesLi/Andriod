package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * The Table for different DroneSettings. currently we only have 1 line in this table where we
 * switch the contents when necessary instead of inserting multiple "settings configurations" but
 * that could be changed
 */
@Entity
public class DroneSetting {
    @PrimaryKey
    @ColumnInfo(name = "settings_id")
    public int settings_id;

    @ColumnInfo(name = "flight_altitude")
    public int flight_altitude;

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
