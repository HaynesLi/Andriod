package com.paltech.dronesncars.computing;

import android.net.Uri;

import com.paltech.dronesncars.model.Result;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * A Mock for the Computer-Vision-Pipeline for the Verkehrsübungsplatz Baldham
 */
public class WeedDetectorMock_Baldham implements WeedDetectorInterface {

    private final List<Result> results;
    private Polygon polygon;

    /**
     * returns 3 fixed GeoPoints on the Verkehrsübungsplatz in Baldham
     * @param locations list of GPS-locations where a picture has been taken
     * @param pictures list of Uris to access the pictures from the phones storage.
     *                 Required: locations.size() == pictures.size() as each location gets paired
     *                 with one picture
     * @return 3 fixed GeoPoints on the Verkehrsübungsplatz in Baldham
     */
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
