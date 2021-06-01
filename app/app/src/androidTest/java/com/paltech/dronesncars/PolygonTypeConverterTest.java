package com.paltech.dronesncars;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.TypeConverters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PolygonTypeConverterTest {

    private TypeConverters type_converter;

    @Before
    public void get_type_converter() {
        type_converter = new TypeConverters();
    }

    @Test
    public void empty_polygon_test() {
        Polygon expected_polygon = new Polygon();

        String json_polygon = type_converter.fromPolygon(expected_polygon);

        Polygon actual_polygon = type_converter.fromPolygonString(json_polygon);

        assertEquals(actual_polygon.getActualPoints(), expected_polygon.getActualPoints());
        assertEquals(actual_polygon.getHoles(), expected_polygon.getHoles());
    }

    @Test
    public void polygon_without_holes() {
        Polygon expected_polygon = new Polygon();

        expected_polygon.addPoint(new GeoPoint(48.29574258901285,
                11.896900532799023));
        expected_polygon.addPoint(new GeoPoint(48.30841764645962,
                11.917242405117028));
        expected_polygon.addPoint(new GeoPoint(48.312927380430466,
                11.894068121549093));

        String json_polygon = type_converter.fromPolygon(expected_polygon);

        Polygon actual_polygon = type_converter.fromPolygonString(json_polygon);

        assertEquals(actual_polygon.getActualPoints(), expected_polygon.getActualPoints());
        assertEquals(actual_polygon.getHoles(), expected_polygon.getHoles());
    }

    @Test
    public void polygon_with_holes() {
        Polygon expected_polygon = new Polygon();

        expected_polygon.addPoint(new GeoPoint(48.29574258901285,
                11.896900532799023));
        expected_polygon.addPoint(new GeoPoint(48.30841764645962,
                11.917242405117028));
        expected_polygon.addPoint(new GeoPoint(48.312927380430466,
                11.894068121549093));

        List<GeoPoint> hole_1 = new ArrayList<>();
        hole_1.add(new GeoPoint(0.0,
                0.0));
        hole_1.add(new GeoPoint(0.0,
                1.0));
        hole_1.add(new GeoPoint(1.0,
                0.0));

        List<GeoPoint> hole_2 = new ArrayList<>();
        hole_2.add(new GeoPoint(1.0,
                0.0));
        hole_2.add(new GeoPoint(0.0,
                1.0));
        hole_2.add(new GeoPoint(1.0,
                1.0));

        List<List<GeoPoint>> holes = new ArrayList<>();
        holes.add(hole_1);
        holes.add(hole_2);

        expected_polygon.setHoles(holes);

        String json_polygon = type_converter.fromPolygon(expected_polygon);

        Polygon actual_polygon = type_converter.fromPolygonString(json_polygon);

        assertEquals(actual_polygon.getActualPoints(), expected_polygon.getActualPoints());
        assertEquals(actual_polygon.getHoles(), expected_polygon.getHoles());
    }

}
