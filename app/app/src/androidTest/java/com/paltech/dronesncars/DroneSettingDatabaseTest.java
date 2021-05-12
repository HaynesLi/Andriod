package com.paltech.dronesncars;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.DroneSetting;
import com.paltech.dronesncars.model.DroneSettingDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class DroneSettingDatabaseTest {
    private DroneSettingDAO droneSettingDAO;
    private DNR_Database database;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        droneSettingDAO = database.getDroneSettingDAO();
    }

    @After
    public void closeDb() throws IOException {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void droneSettingsTest() throws Exception {
        DroneSetting droneSetting= new DroneSetting(111, 2);

        droneSettingDAO.insertSetting(droneSetting);

        DroneSetting fromDatabase = droneSettingDAO.getDroneSettingByID(droneSetting.settings_id);

        assertEquals(fromDatabase, droneSetting);
        assertEquals(droneSetting.flight_altitude, fromDatabase.flight_altitude);
        assertEquals(droneSetting.flight_altitude, droneSettingDAO.getFlightAltitude(droneSetting.settings_id));

        droneSetting.flight_altitude = 3;
        droneSettingDAO.updateSetting(droneSetting);
        assertEquals(droneSetting.flight_altitude, droneSettingDAO.getFlightAltitude(droneSetting.settings_id));

        droneSettingDAO.delete(droneSetting);

        assertNull(droneSettingDAO.getDroneSettingByID(droneSetting.settings_id));
    }
}
