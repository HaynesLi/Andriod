package com.paltech.dronesncars;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class RoverDatabaseTest {
    private RoverDAO roverDAO;
    private DNR_Database database;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        roverDAO = database.getRoverDAO();
    }

    @After
    public void closeDb() {
            database.clearAllTables();
            database.close();
    }

    @Test
    public void test_get_ids() throws UnknownHostException {

        Rover rover_1 = new Rover(111, InetAddress.getLoopbackAddress());
        rover_1.ip_address = InetAddress.getByName("127.0.0.1");
        rover_1.roverName = "Hubert";
        Rover rover_2 = new Rover(110, InetAddress.getLoopbackAddress());
        rover_2.ip_address = InetAddress.getByName("127.0.0.1");
        rover_2.roverName = "Hubert";

        roverDAO.insertMultipleRovers(rover_1, rover_2);

        List<Integer> ids = roverDAO.get_all_ids_not_livedata();
        assertNotNull(ids);
        assert(ids.get(0) == 110);
        assert(ids.get(1) == 111);
    }


    public void roverTest() {
        Rover rover_1 = new Rover(111, InetAddress.getLoopbackAddress());
        Rover rover_2 = new Rover(110, InetAddress.getLoopbackAddress());

        roverDAO.insertMultipleRovers(rover_1, rover_2);

        List<Rover> result = roverDAO.getAll();

        assertEquals(result.size(), 2);
        assert(result.contains(rover_1));
        assert(result.contains(rover_2));

        Rover rover_1_result = roverDAO.getRoverByID(rover_1.rover_id);
        assertEquals(rover_1_result, rover_1);
        assertEquals(rover_1_result.roverName, rover_1.roverName);
        assertEquals(rover_1_result.battery, rover_1.battery, 0.0);

        roverDAO.delete(rover_1);
        roverDAO.delete(rover_2);
        assert(roverDAO.getAll().isEmpty());
    }
}
