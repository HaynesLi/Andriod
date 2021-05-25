package com.paltech.dronesncars.computing;

import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TestPointInPolygon {

    private final double STD_DIST_LAT = 111.3;
    private final double STD_DIST_LONG = 71.5;

    //latitude (x) ist VORNE, longitude HINTEN (y)

    // ? method from robins code
    static boolean intersects(GeoPoint A, GeoPoint B, GeoPoint P) throws Exception{
        if (A.getLongitude() > B.getLongitude())
            return intersects(B, A, P);

        if (P.getLongitude() == A.getLongitude() || P.getLongitude() == B.getLongitude())
            throw new Exception("fuck you");

        if (P.getLongitude() > B.getLongitude() || P.getLongitude() < A.getLongitude() || P.getLatitude() >= max(A.getLatitude(), B.getLatitude()))
            return false;

        if (P.getLatitude() < min(A.getLatitude(), B.getLatitude()))
            return true;

        double red = (P.getLongitude() - A.getLongitude()) / (double) (P.getLatitude() - A.getLatitude());
        double blue = (B.getLongitude() - A.getLongitude()) / (double) (B.getLatitude() - A.getLatitude());
        return red >= blue;
    }

    // ? method from robins code to check whether a GeoPoint is in a polygon
    static boolean contains(List<GeoPoint> shape, GeoPoint pnt) throws Exception{
        boolean inside = false;
        int len = shape.size();
        for (int i = 0; i < len; i++) {
            if (intersects(shape.get(i), shape.get((i + 1) % len), pnt))
                inside = !inside;
        }
        return inside;
    }


    // method to compute all points in the polygon and where the drone is gonna stop and take photos
    // TODO clean this code up and put it on a background thread
    public GeoPoint[][] checkPoints(Polygon polygon, double distance) {
        BoundingBox boundingBox = polygon.getBounds();
        double distance_latitude = Math.abs(boundingBox.getLatNorth() - boundingBox.getLatSouth());
        double distance_longitude = Math.abs(boundingBox.getLonEast() - boundingBox.getLonWest());


        double distance_latitude_m = distance / STD_DIST_LAT;
        double distance_longitude_m = distance / STD_DIST_LONG;

        GeoPoint[][] ourPoints = new GeoPoint[(int)(distance_latitude/distance_latitude_m)]
                [(int)(distance_longitude/distance_longitude_m)];

        double min_latitude = min(boundingBox.getLatNorth(), boundingBox.getLatSouth());
        double min_longitude = min(boundingBox.getLonEast(), boundingBox.getLonWest());



        for (int x = 0; x < ourPoints.length; x++) {
            for (int y = 0; y < ourPoints[0].length; y++) {
                GeoPoint current_point = new GeoPoint(x*distance_latitude_m+min_latitude, y*distance_longitude_m+min_longitude);
                ourPoints[x][y] = null;
                try {
                    if (contains(polygon.getActualPoints(), current_point)) {
                        ourPoints[x][y] = current_point;
                    }
                } catch (Exception e) {
                    Log.d("DEBUG_POINTS", "checkPoints: " + e.getMessage());
                }
            }
        }


        return ourPoints;

    }

}
