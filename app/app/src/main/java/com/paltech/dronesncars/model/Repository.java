package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.views.overlay.Polygon;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class Repository {

    private Context context;
    private Executor executor;


    private KMLParser kmlParser;

    private Dictionary<String, Polygon> polygonsToChoose;

    private final DNR_Database database;
    private final RoverDAO roverDAO;
    private final ResultDAO resultDAO;
    private final DroneSettingDAO droneSettingDAO;
    private final MapDAO mapDAO;
    private final RoverRouteDAO roverRouteDAO;
    private final RoverRoutineDAO roverRoutineDAO;
    private int current_routine_id = -1;


    @Inject
    public Repository(@ApplicationContext Context context, Executor executor) {
        database = DNR_Database.getInstance(context);
        resultDAO = database.getResultDAO();
        roverDAO = database.getRoverDAO();
        droneSettingDAO = database.getDroneSettingDAO();
        mapDAO = database.getMapDAO();
        roverRouteDAO = database.getRoverRouteDAO();
        roverRoutineDAO = database.getRoverRoutineDAO();
        this.context = context;
        this.executor = executor;
    }

    public List<Rover> getRovers() {
        return roverDAO.getAll();
    }

    public List<Result> getResults() {
        return resultDAO.getAllResults();
    }

    public List<RoverRoute> getRoutes() {
        return roverRouteDAO.getAllRoverRoutes();
    }

    public List<RoverRoutine> getRoutines() {
        return roverRoutineDAO.getAllRoverRoutines();
    }

    public RoverRoutine getCurrentRoutine() {
        if (current_routine_id == -1) {
            return null;
        }
        return roverRoutineDAO.getRoverRoutineByID(current_routine_id);
    }

    public void parseKMLFile(Uri kml_file_uri, ViewModelCallback<Dictionary<String, Polygon>> mapViewModelCallback) {
        executor.execute(() -> {
            polygonsToChoose = KMLParser.parseKMLFile(kml_file_uri, context);
            mapViewModelCallback.onComplete(polygonsToChoose);
        });
    }

    public Dictionary<String, Polygon> getPolygonsToChoose() {
        if (polygonsToChoose == null) {
            polygonsToChoose = new Hashtable<>();
        }
        return polygonsToChoose;
    }

    // TODO add more stuff here...
}
