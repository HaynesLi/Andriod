package com.paltech.dronesncars;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.PolygonModel;
import com.paltech.dronesncars.model.PolygonModelDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PolygonModelDatabaseTest {
    private DNR_Database database;
    private PolygonModelDAO polygonModelDAO;

    @Before
    public void openDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        polygonModelDAO = database.getPolygonModelDAO();
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void polygonModelDatabaseTest() {
        Polygon polygon = new Polygon();
        polygon.addPoint(new GeoPoint(48.29574258901285, 11.896900532799023));
        polygon.addPoint(new GeoPoint(48.30841764645962, 11.917242405117028));
        PolygonModel expectedPolygonModel = new PolygonModel(1, polygon);

        polygonModelDAO.insertPolygonModel(expectedPolygonModel);
        PolygonModel actualPolygonModel = polygonModelDAO.getPolygonModelByID(expectedPolygonModel.polygon_id);

        assertEquals(actualPolygonModel, expectedPolygonModel);
        assert(polygonEquals(actualPolygonModel.polygon, expectedPolygonModel.polygon));

        Polygon newPolygon = new Polygon();
        newPolygon.addPoint(new GeoPoint(48.29574258901285, 11.896900532799023));
        newPolygon.addPoint(new GeoPoint(48.30841764645962, 11.917242405117028));
        newPolygon.addPoint(new GeoPoint(48.312927380430466, 11.894068121549093));

        expectedPolygonModel.polygon = newPolygon;

        polygonModelDAO.updatePolygonModel(expectedPolygonModel);
        actualPolygonModel = polygonModelDAO.getPolygonModelByID(expectedPolygonModel.polygon_id);

        assertEquals(actualPolygonModel, expectedPolygonModel);
        assert(polygonEquals(actualPolygonModel.polygon, expectedPolygonModel.polygon));

        polygonModelDAO.deletePolygonModel(expectedPolygonModel);
    }

    @Test
    public void NullPolygonDatabaseTest() {
        PolygonModel expectedPolygonModel = new PolygonModel(1, null);

        polygonModelDAO.insertPolygonModel(expectedPolygonModel);
        PolygonModel actualPolygonModel = polygonModelDAO.getPolygonModelByID(expectedPolygonModel.polygon_id);

        assertEquals(actualPolygonModel, expectedPolygonModel);
        assert(polygonEquals(actualPolygonModel.polygon, expectedPolygonModel.polygon));
    }

    private boolean polygonEquals(Polygon polygon_1, Polygon polygon_2) {
        if (polygon_1 == polygon_2) return true;
        if (polygon_1 == null || Polygon.class != polygon_1.getClass()) return false;
        if (polygon_2 == null || Polygon.class != polygon_2.getClass()) return false;

        return polygon_1.getActualPoints().equals(polygon_2.getActualPoints());
    }
}
