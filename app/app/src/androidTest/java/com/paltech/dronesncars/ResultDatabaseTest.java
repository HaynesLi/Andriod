package com.paltech.dronesncars;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.paltech.dronesncars.model.DNR_Database;
import com.paltech.dronesncars.model.Result;
import com.paltech.dronesncars.model.ResultDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ResultDatabaseTest {
    private ResultDAO resultDAO;
    private DNR_Database database;

    @Before
    public void openDatabase() throws IOException {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, DNR_Database.class).build();
        resultDAO = database.getResultDAO();
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void resultDatabaseTest() {
        Result expected_result = new Result(1, 0.4);

        resultDAO.insertMultipleResults(expected_result);

        List<Result> actual_results = resultDAO.getAllResults();

        assertEquals(actual_results.size(), 1);

        Result actual_result = actual_results.get(0);
        assertEquals(actual_result, expected_result);
        assertEquals(actual_result.certainty, expected_result.certainty, 0.0);
    }
}
