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
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void closeDb() throws IOException {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void roverTest() throws Exception {
        Rover rover_1 = new Rover(111, "Nautilus", 0.5);
        Rover rover_2 = new Rover(110, "HMS Victory", 0.99);

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
