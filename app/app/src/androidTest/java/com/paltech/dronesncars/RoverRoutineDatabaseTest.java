package com.paltech.dronesncars;

import android.content.Context;
import android.util.ArraySet;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverDAO;
import com.paltech.dronesncars.model.RoverRoute;
import com.paltech.dronesncars.model.RoverRouteDAO;
import com.paltech.dronesncars.model.RoverRoutine;
import com.paltech.dronesncars.model.RoverRoutineDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RoverRoutineDatabaseTest {
    private RoverRouteDAO roverRouteDAO;
    private DNR_Database database;
    private RoverDAO roverDAO;
    private RoverRoutineDAO roverRoutineDAO;

    @Before
    public void openDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        roverRouteDAO = database.getRoverRouteDAO();
        roverDAO = database.getRoverDAO();
        roverRoutineDAO = database.getRoverRoutineDAO();
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void roverRoutineDatabaseTest() {
        Rover rover = new Rover(1);
        RoverRoutine expected_roverRoutine = new RoverRoutine(3, 1);

        List<GeoPoint> route = new ArrayList<>();
        route.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        route.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        route.add(new GeoPoint(48.312927380430466, 11.894068121549093));

        RoverRoute roverRoute_1 = new RoverRoute(1, rover.rover_id, route,
                expected_roverRoutine.rover_routine_id);
        RoverRoute roverRoute_2 = new RoverRoute(2, rover.rover_id, route,
                expected_roverRoutine.rover_routine_id);

        roverDAO.insertMultipleRovers(rover);
        roverRoutineDAO.insert(expected_roverRoutine);
        roverRouteDAO.insertMultiple(roverRoute_1, roverRoute_2);


        RoverRoutine actual_roverRoutine = roverRoutineDAO.getRoverRoutineByID(expected_roverRoutine.rover_routine_id);
        assertEquals(actual_roverRoutine, expected_roverRoutine);
        assertEquals(actual_roverRoutine.num_of_rovers, expected_roverRoutine.num_of_rovers);
    }

    @Test
    public void roverRoutineRouteRelationshipDatabaseTest() {
        Rover rover = new Rover(1);
        RoverRoutine expected_roverRoutine = new RoverRoutine(3, 1);

        List<GeoPoint> route = new ArrayList<>();
        route.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        route.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        route.add(new GeoPoint(48.312927380430466, 11.894068121549093));

        RoverRoute roverRoute_1 = new RoverRoute(1, rover.rover_id, route,
                expected_roverRoutine.rover_routine_id);
        RoverRoute roverRoute_2 = new RoverRoute(2, rover.rover_id, route,
                expected_roverRoutine.rover_routine_id);
        Set<RoverRoute> expected_routes = new ArraySet<>();
        expected_routes.add(roverRoute_1);
        expected_routes.add(roverRoute_2);

        roverDAO.insertMultipleRovers(rover);
        roverRoutineDAO.insert(expected_roverRoutine);
        roverRouteDAO.insertMultiple(roverRoute_1, roverRoute_2);


        List<RoverRoute> actual_routes = roverRouteDAO.findRoverRoutesForRoutine(expected_roverRoutine.rover_routine_id);
        assertEquals(actual_routes.size(), 2);
        assertEquals(new ArraySet<>(actual_routes), expected_routes);
    }
}
