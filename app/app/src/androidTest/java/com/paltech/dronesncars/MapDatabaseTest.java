package com.paltech.dronesncars;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.Map;
import com.paltech.dronesncars.model.MapDAO;
import com.paltech.dronesncars.model.RoverRoutine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class MapDatabaseTest {
    private MapDAO mapDAO;
    private DNR_Database database;

    @Before
    public void openDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        mapDAO = database.getMapDAO();
    }

    @After
    public void closeDatabase(){
        database.close();
    }

    @Test
    public void mapDatabaseTest() throws Exception {
        int rover_routine_id = 1;

        Polygon polygon = new Polygon();

        List<GeoPoint> drone_route = new ArrayList<>();
        drone_route.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        drone_route.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        drone_route.add(new GeoPoint(48.312927380430466, 11.894068121549093));

        for (GeoPoint point : drone_route){
            polygon.addPoint(point);
        }

        Map map = new Map(2, polygon, drone_route, rover_routine_id);

        mapDAO.insertMap(map);

        List<Map> map_results = mapDAO.getAllMaps();
        assertEquals(map_results.size(), 1);

        Map map_result = map_results.get(0);
        assertEquals(map_result, map);
        assert(polygonEquals(map_result.polygon, map.polygon));
        assertEquals(map_result.drone_route, map.drone_route);
        assertEquals(map_result.rover_routine_id, rover_routine_id);

        mapDAO.deleteMap(map);
        assert(mapDAO.getAllMaps().isEmpty());
    }

    private boolean polygonEquals(Polygon polygon_1, Polygon polygon_2) {
        if (polygon_1 == polygon_2) return true;
        if (polygon_1 == null || Polygon.class != polygon_1.getClass()) return false;
        if (polygon_2 == null || Polygon.class != polygon_2.getClass()) return false;

        return polygon_1.getActualPoints().equals(polygon_2.getActualPoints());
    }
}
