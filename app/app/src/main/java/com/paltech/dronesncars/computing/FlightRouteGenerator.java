package com.paltech.dronesncars.computing;

import android.util.Log;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class FlightRouteGenerator {

    private static final double STD_DIST_LAT = 111.3;
    private static final double STD_DIST_LONG = 71.5;

    /**
     * ?
     *
     * @param A
     * @param B
     * @param P
     * @return
     * @throws RuntimeException
     */
    static boolean intersects(GeoPoint A, GeoPoint B, GeoPoint P) throws RuntimeException {
        if (A.getLongitude() > B.getLongitude())
            return intersects(B, A, P);

        if (P.getLongitude() == A.getLongitude() || P.getLongitude() == B.getLongitude())
            throw new RuntimeException("P lies on the shapes outline?"); // TODO what is this?!

        if (P.getLongitude() > B.getLongitude() || P.getLongitude() < A.getLongitude() || P.getLatitude() >= max(A.getLatitude(), B.getLatitude()))
            return false;

        if (P.getLatitude() < min(A.getLatitude(), B.getLatitude()))
            return true;

        double red = (P.getLongitude() - A.getLongitude()) / (double) (P.getLatitude() - A.getLatitude());
        double blue = (B.getLongitude() - A.getLongitude()) / (double) (B.getLatitude() - A.getLatitude());
        return red >= blue;
    }

    /**
     * Method to test whether a GeoPoint is inside the given shape, while considering holes in the
     * polygon
     *
     * @param shapes List of Lists of GeoPoints corresponding to the outline of our polygon.
     *               For example first shape is the polygon's outline, second is a hole's outline
     *               inside the polygon
     * @param pnt   the GeoPoint that is being tested
     * @return true: shape contains pnt | false: shape does not contain pnt
     * @throws RuntimeException if the picked GeoPoint was on one of our outlines
     */
    static boolean contains(List<List<GeoPoint>> shapes, GeoPoint pnt) throws  RuntimeException {
        boolean inside = false;
        for (List<GeoPoint> shape: shapes) {
            int len = shape.size();
            for (int i = 0; i < len; i++) {
                if (intersects(shape.get(i), shape.get((i + 1) % len), pnt))
                    inside = !inside;
            }
        }
        return inside;
    }

    /**
     * Method can be used to compute all the GeoPoints inside a Polygon which need to be visited for
     * a certain set of settings of the drone
     *
     * @param polygon  The polygon which has to be considered (osmdroid.Polygon)
     * @param distance the distance in the grid of squares between different points
     * @return a 2-dimensional array that contains Null if the point in the grid is outside of the
     * polygon or the osmdroid.GeoPoint if the corresponding point is inside it
     */
    private static GeoPoint[][] get_targets_for_polygon(Polygon polygon, double distance) {
        BoundingBox boundingBox = polygon.getBounds();
        double distance_latitude = Math.abs(boundingBox.getLatNorth() - boundingBox.getLatSouth());
        double distance_longitude = Math.abs(boundingBox.getLonEast() - boundingBox.getLonWest());


        double distance_latitude_m = distance / STD_DIST_LAT;
        double distance_longitude_m = distance / STD_DIST_LONG;

        GeoPoint[][] ourPoints = new GeoPoint[(int) (distance_latitude / distance_latitude_m)]
                [(int) (distance_longitude / distance_longitude_m)];

        double min_latitude = min(boundingBox.getLatNorth(), boundingBox.getLatSouth());
        double min_longitude = min(boundingBox.getLonEast(), boundingBox.getLonWest());

        List<List<GeoPoint>> shapes = new ArrayList<>();
        shapes.add(polygon.getActualPoints());
        shapes.addAll(polygon.getHoles());

        for (int x = 0; x < ourPoints.length; x++) {
            for (int y = 0; y < ourPoints[0].length; y++) {
                GeoPoint current_point = new GeoPoint(x * distance_latitude_m + min_latitude, y * distance_longitude_m + min_longitude);
                ourPoints[x][y] = null;
                try {
                    if (contains(shapes, current_point)) {
                        ourPoints[x][y] = current_point;
                    }
                } catch (Exception e) {
                    Log.d("DEBUG_POINTS", "checkPoints: " + e.getMessage());
                }
            }
        }


        return ourPoints;
    }

    /**
     * Method can be used to compute the order in which the target points will be visited by the
     * drone
     *
     * @param targets a 2-dimensional array representing a grid of squares with GeoPoints (the
     *                targets) at the intersections. Contains Null if the corresponding GeoPoint is
     *                outside of the target polygon
     * @return an ordered list of GeoPoints to visit in this order (and take photos)
     */
    private static List<GeoPoint> compute_route_from_targets(GeoPoint[][] targets) {
        List<GeoPoint> route = new ArrayList<>();
        for (GeoPoint[] row : targets) {
            for (GeoPoint point : row) {
                if (point != null) {
                    route.add(point);
                }
            }
        }
        return route;
    }

    public static List<GeoPoint> compute_flight_route(Polygon polygon, int distance) {
        GeoPoint[][] targets = get_targets_for_polygon(polygon, distance*0.001);
        List<GeoPoint> flightroute = compute_route_from_targets(targets);
        return flightroute;
    }

}
