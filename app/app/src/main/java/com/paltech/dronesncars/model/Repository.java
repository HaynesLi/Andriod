package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;

import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.views.overlay.Polygon;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class Repository {

    private final Context context;
    private final Executor executor;


    private Dictionary<String, Polygon> polygonsToChoose;

    private final RoverDAO roverDAO;
    private final ResultDAO resultDAO;
    private final DroneSettingDAO droneSettingDAO;
    private final MapDAO mapDAO;
    private final RoverRouteDAO roverRouteDAO;
    private final RoverRoutineDAO roverRoutineDAO;
    private final PolygonModelDAO polygonModelDAO;


    private final int POLYGON_ID = 1;
    private int ROUTINE_ID = 1;
    private final int DRONE_SETTING_ID = 1;


    @Inject
    public Repository(@ApplicationContext Context context, Executor executor) {
        DNR_Database database = DNR_Database.getInstance(context);
        resultDAO = database.getResultDAO();
        roverDAO = database.getRoverDAO();
        droneSettingDAO = database.getDroneSettingDAO();
        mapDAO = database.getMapDAO();
        roverRouteDAO = database.getRoverRouteDAO();
        roverRoutineDAO = database.getRoverRoutineDAO();
        polygonModelDAO = database.getPolygonModelDAO();
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
        if (ROUTINE_ID == -1) {
            return null;
        }
        return roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
    }

    public void parseKMLFile(Uri kml_file_uri, ViewModelCallback<Dictionary<String, Polygon>> mapViewModelCallback) {
        executor.execute(() -> {
            polygonsToChoose = KMLParser.parseKMLFile(kml_file_uri, context);
            mapViewModelCallback.onComplete(polygonsToChoose);
        });
    }

    public void clearSelectablePolygons(ViewModelCallback<Dictionary<String, Polygon>> mapViewModelCallback) {
        polygonsToChoose = new Hashtable<>();
        mapViewModelCallback.onComplete(polygonsToChoose);
    }

    public Dictionary<String, Polygon> getPolygonsToChoose() {
        if (polygonsToChoose == null) {
            polygonsToChoose = new Hashtable<>();
        }
        return polygonsToChoose;
    }

    public void setPolygon(Polygon polygon, ViewModelCallback<Polygon> mapViewModelCallback) {
        executor.execute(() -> {
            polygonModelDAO.insertPolygonModel(new PolygonModel(POLYGON_ID, polygon));
            mapViewModelCallback.onComplete(polygon);
        });
    }

    public void getPolygon(ViewModelCallback<Polygon> mapViewModelCallback) {
        executor.execute(() -> {
            PolygonModel polygonModel = polygonModelDAO.getPolygonModelByID(POLYGON_ID);
            if (polygonModel == null) {
                mapViewModelCallback.onComplete(null);
            } else {
                mapViewModelCallback.onComplete(polygonModel.polygon);
            }
        });
    }

    public void clearPolygon(ViewModelCallback<Polygon> mapViewModelCallback) {
        executor.execute(() -> {
            PolygonModel comparable = new PolygonModel(POLYGON_ID, null);
            polygonModelDAO.deletePolygonModel(comparable);
            mapViewModelCallback.onComplete(null);
        });
    }

    public void setFlightAltitude(int altitude, ViewModelCallback<Integer> droneSettingCallback) {
        executor.execute(() -> {
            DroneSetting currentSetting = droneSettingDAO.getDroneSettingByID(DRONE_SETTING_ID);
            if (currentSetting != null) {
                currentSetting.flight_altitude = altitude;
                droneSettingDAO.updateSetting(currentSetting);
            } else {
                currentSetting = new DroneSetting(DRONE_SETTING_ID, altitude);
                droneSettingDAO.insertSetting(currentSetting);
            }
            droneSettingCallback.onComplete(altitude);
        });
    }

    public void getFlightAltitude(ViewModelCallback<Integer> droneSettingCallback) {
        executor.execute(() -> {
            DroneSetting currentSetting = droneSettingDAO.getDroneSettingByID(DRONE_SETTING_ID);
            if(currentSetting == null) {
                droneSettingCallback.onComplete(0);
            } else {
                droneSettingCallback.onComplete(currentSetting.flight_altitude);
            }
        });
    }
}
