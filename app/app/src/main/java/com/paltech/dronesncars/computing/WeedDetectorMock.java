package com.paltech.dronesncars.computing;

import android.net.Uri;

import com.paltech.dronesncars.model.Result;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A Mock of the Computer-Vision-Pipeline
 */
public class WeedDetectorMock implements WeedDetectorInterface {

    private Polygon polygon;
    private final int MAX_OFFSET = 50;
    private final int MIN_OFFSET = 1;


    /**
     * Mocks the method of the WeedDetectorInterface by returning
     * min(10, polygon.getActualPoints().size()) results from within the specified polygon.
     *
     * @param locations list of GPS-locations where a picture has been taken
     * @param pictures list of Uris to access the pictures from the phones storage.
     *                 Required: locations.size() == pictures.size() as each location gets paired
     *                 with one picture
     * @return returns a List of at max 10 results at random locations inside the specified polygon
     */
    @Override
    public List<Result> get_results_from_pictures(List<GeoPoint> locations, List<Uri> pictures) {

        if(this.polygon == null || locations.size() != pictures.size()) {
            return new ArrayList<>();
        }

        List<Result> results = new ArrayList<>();
        List<GeoPoint> polygon_points = polygon.getActualPoints();
        List<GeoPoint> used_points = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < Math.min(10, polygon_points.size()); i++) {
            GeoPoint random_geopoint;
            do {
                random_geopoint = polygon_points.get(random.nextInt(polygon_points.size()));
            } while(used_points.contains(random_geopoint));

            GeoPoint current_point;
            do {
                double longitude_offset = random.nextInt(MAX_OFFSET - MIN_OFFSET) + MIN_OFFSET;
                double latitude_offset = random.nextInt(MAX_OFFSET - MIN_OFFSET) + MIN_OFFSET;

                longitude_offset = FlightRouteGenerator.meter_to_longitude_distance(longitude_offset);
                latitude_offset = FlightRouteGenerator.meter_to_latitude_distance(latitude_offset);

                current_point = new GeoPoint(
                        random_geopoint.getLatitude() + latitude_offset,
                        random_geopoint.getLongitude() + longitude_offset
                );

                //TODO this isn't exactly elegant code

                if (polygon_does_not_contain(current_point)) {
                    current_point = new GeoPoint(
                            random_geopoint.getLatitude() - latitude_offset,
                            random_geopoint.getLongitude() + longitude_offset
                        );
                } else {
                    results.add(new Result(i, random.nextDouble(), current_point));
                    used_points.add(random_geopoint);
                    continue;
                }
                if (polygon_does_not_contain(current_point)) {
                    current_point = new GeoPoint(
                            random_geopoint.getLatitude() + latitude_offset,
                            random_geopoint.getLongitude() - longitude_offset
                    );
                } else {
                    results.add(new Result(i, random.nextDouble(), current_point));
                    used_points.add(random_geopoint);
                    continue;
                }
                if (polygon_does_not_contain(current_point)) {
                    current_point = new GeoPoint(
                            random_geopoint.getLatitude() - latitude_offset,
                            random_geopoint.getLongitude() - longitude_offset
                    );
                } else {
                    results.add(new Result(i, random.nextDouble(), current_point));
                    used_points.add(random_geopoint);
                    continue;
                }

                if (!polygon_does_not_contain(current_point)) {
                    results.add(new Result(i, random.nextDouble(), current_point));
                    used_points.add(random_geopoint);
                }

            } while (polygon_does_not_contain(current_point));
        }

        return results;
    }

    /**
     * a local wrapper for the FlightGenerator.contains(...) method, which also takes care of
     * catching its RuntimeException. Checks whether the given point lies inside our polygon
     *
     * @param point the geopoint to check
     * @return true if point lies inside the specified polygon, false otherwise
     */
    private boolean polygon_does_not_contain(GeoPoint point) {
        List<List<GeoPoint>> shapes = new ArrayList<>();
        shapes.add(polygon.getActualPoints());
        shapes.addAll(polygon.getHoles());

        try {
            return !FlightRouteGenerator.contains(shapes, point);
        } catch (RuntimeException runtime_exception) {
            return true;
        }
    }

    /**
     * A Mock for the interface WeedDetectorInterface as this task is computer vision task is not
     * part of our project
     *
     * @param polygon the polygon to choose the result's random gps locations from
     */
    public WeedDetectorMock(Polygon polygon) {
        this.polygon = polygon;
    }

    /**
     * A Mock for the interface WeedDetectorInterface as this task is computer vision task is not
     * part of our project
     */
    public WeedDetectorMock() {
    }

    /**
     * Set the WeedDetectors polygon to a new value.
     * The polygon will be used to pick the random gps locations of the mocked-results
     *
     * @param polygon the polygon to set the local polygon to
     */
    public void set_polygon(Polygon polygon) {
        this.polygon = polygon;
    }
}
