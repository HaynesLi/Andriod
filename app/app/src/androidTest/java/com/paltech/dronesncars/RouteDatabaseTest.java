package com.paltech.dronesncars;

import android.content.Context;

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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RouteDatabaseTest {
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
    public void routeDatabaseTest() {
        Rover rover = new Rover(1, InetAddress.getLoopbackAddress());
        RoverRoutine roverRoutine = new RoverRoutine(3, 1);

        List<GeoPoint> route = new ArrayList<>();
        route.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        route.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        route.add(new GeoPoint(48.312927380430466, 11.894068121549093));

        RoverRoute expected_roverRoute = new RoverRoute("1", 1, route,
                roverRoutine.rover_routine_id);

        roverDAO.insertMultipleRovers(rover);
        roverRoutineDAO.insert(roverRoutine);

        roverRouteDAO.insertMultiple(expected_roverRoute);
        List<RoverRoute> actual_roverRoutes = roverRouteDAO.getAllRoverRoutes();

        assertEquals(actual_roverRoutes.size(), 1);

        RoverRoute actual_roverRoute = actual_roverRoutes.get(0);

        assertEquals(actual_roverRoute, expected_roverRoute);
        assertEquals(actual_roverRoute.routine_id, expected_roverRoute.routine_id);
        assertEquals(actual_roverRoute.corresponding_rover_id, expected_roverRoute.corresponding_rover_id);
        assertEquals(actual_roverRoute.route, expected_roverRoute.route);
    }

    @Test
    public void routeRoverRelationshipDatabaseTest() {
        Rover rover_1;
        Rover rover_2;
        InetAddress inet_address = InetAddress.getLoopbackAddress();
        rover_1 = new Rover(1, inet_address);
        rover_2 = new Rover(32, inet_address);

        RoverRoutine roverRoutine = new RoverRoutine(3, 1);

        List<GeoPoint> route = new ArrayList<>();
        route.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        route.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        route.add(new GeoPoint(48.312927380430466, 11.894068121549093));

        RoverRoute expected_roverRoute = new RoverRoute("1", rover_1.rover_id, route,
                roverRoutine.rover_routine_id);

        roverDAO.insertMultipleRovers(rover_1, rover_2);
        roverRoutineDAO.insert(roverRoutine);
        roverRouteDAO.insertMultiple(expected_roverRoute);

        Rover expected_rover = roverRouteDAO.getRoverForRoute(expected_roverRoute.rover_route_id);
        assertEquals(expected_rover, rover_1);

        List<RoverRoute> expected_routes = roverDAO.getRoutesForRover(rover_1.rover_id);
        assertEquals(expected_routes.size(), 1);
        assertEquals(expected_routes.get(0), expected_roverRoute);

        List<RoverRoute> expected_empty_routes = roverDAO.getRoutesForRover(rover_2.rover_id);
        assert(expected_empty_routes.isEmpty());
    }



}
