package com.paltech.dronesncars.computing;

import android.net.Uri;

import com.paltech.dronesncars.model.Result;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class WeedDetectorMock_Baldham implements WeedDetectorInterface {

    private final List<Result> results;
    private Polygon polygon;

    @Override
    public List<Result> get_results_from_pictures(List<GeoPoint> locations, List<Uri> pictures) {
        return results;
    }

    public WeedDetectorMock_Baldham(Polygon polygon) {
        this.polygon = polygon;
        this.results = new ArrayList<>();
        results.add(new Result(0, 1.0, new GeoPoint(48.108957, 11.778282)));
        results.add(new Result(1, 1.0, new GeoPoint(48.108968, 11.778261)));
        results.add(new Result(2, 1.0, new GeoPoint(48.108993, 11.778274)));
    }
}
