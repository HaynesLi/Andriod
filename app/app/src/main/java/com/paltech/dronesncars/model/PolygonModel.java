package com.paltech.dronesncars.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.views.overlay.Polygon;

import java.util.Objects;

/**
 * The table we use to save the one polygon we display on the map. Similar to {@link DroneSetting}
 * we only save one and replace it when necessary instedd of saving multiple and selecting the
 * newest one.
 */
@Entity
public class PolygonModel {
    @PrimaryKey
    @ColumnInfo(name="polygon_id")
    public int polygon_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolygonModel that = (PolygonModel) o;
        return polygon_id == that.polygon_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(polygon_id);
    }

    @ColumnInfo(name = "polygon")
    public Polygon polygon;

    public PolygonModel(int polygon_id, Polygon polygon) {
        this.polygon_id = polygon_id;
        this.polygon = polygon;
    }
}
