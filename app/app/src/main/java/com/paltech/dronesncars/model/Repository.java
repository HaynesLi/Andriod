package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.paltech.dronesncars.computing.FlightRouteGenerator;
import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.util.GeoPoint;
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
    private final FlightRouteDAO flightRouteDAO;
    private final RoverRouteDAO roverRouteDAO;
    private final RoverRoutineDAO roverRoutineDAO;
    private final PolygonModelDAO polygonModelDAO;


    private final int POLYGON_ID = 1;
    private int ROUTINE_ID = 1;
    private final int DRONE_SETTING_ID = 1;
    private final int FLIGHT_ROUTE_ID = 1;


    @Inject
    public Repository(@ApplicationContext Context context, Executor executor) {
        DNR_Database database = DNR_Database.getInstance(context);
        resultDAO = database.getResultDAO();
        roverDAO = database.getRoverDAO();
        droneSettingDAO = database.getDroneSettingDAO();
        flightRouteDAO = database.getFlightRouteDAO();
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
            if (currentSetting == null) {
                droneSettingCallback.onComplete(0);
            } else {
                droneSettingCallback.onComplete(currentSetting.flight_altitude);
            }
        });
    }

    public void setNumOfRovers(int numOfRovers, ViewModelCallback<Integer> roverSettingCallback) {
        executor.execute(() -> {
            RoverRoutine roverRoutine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
            if (roverRoutine != null) {
                roverRoutine.num_of_rovers = numOfRovers;
                roverRoutineDAO.update(roverRoutine);
            } else {
                roverRoutine = new RoverRoutine(ROUTINE_ID, numOfRovers);
                roverRoutineDAO.insert(roverRoutine);
            }
            roverSettingCallback.onComplete(numOfRovers);
        });
    }

    public void getNumOfRovers(ViewModelCallback<Integer> roverSettingCallback) {
        executor.execute(() -> {
            RoverRoutine roverRoutine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
            if (roverRoutine == null) {
                roverSettingCallback.onComplete(0);
            } else {
                roverSettingCallback.onComplete(roverRoutine.num_of_rovers);
            }
        });
    }

    public LiveData<List<Rover>> getCurrentRovers() {
        return roverDAO.getAllLiveData();
    }

    public void setCurrentRovers(List<Rover> currentRovers) {
        executor.execute(() -> {
            roverDAO.deleteAllRovers();
            Rover[] current_rovers_as_array = new Rover[currentRovers.size()];
            current_rovers_as_array = currentRovers.toArray(current_rovers_as_array);
            roverDAO.insertMultipleRovers(current_rovers_as_array);
        });
    }

    public void save_rover_progress(Rover rover, double progress) {
        double add_progress = progress;
        if (rover.progress + add_progress > 1.0) {
            add_progress = 1 - rover.progress;
        }
        if (add_progress > 0) {
            rover.progress += add_progress;
            executor.execute(() -> roverDAO.update(rover));
        }
    }

    public void mock_progress_update() {
        executor.execute(() -> {
            List<Rover> current_rovers = roverDAO.getAll();
            if (current_rovers.size() > 0) {
                save_rover_progress(current_rovers.get(0), 0.1);
            }
        });
    }

    public LiveData<FlightRoute> get_current_flightroute() {
        return flightRouteDAO.get_flightroute_from_id_livedata(FLIGHT_ROUTE_ID);
    }

    public void computeRoute() {
        executor.execute(() -> {
            PolygonModel polygon_model = polygonModelDAO.getPolygonModelByID(POLYGON_ID);
            // TODO: add distance (in meters) between photos as setting
            DroneSetting currentSetting = droneSettingDAO.getDroneSettingByID(DRONE_SETTING_ID);

            if (polygon_model != null && polygon_model.polygon != null && currentSetting != null) {
                List<GeoPoint> route =
                        FlightRouteGenerator.compute_flight_route(polygon_model.polygon, 10);

                FlightRoute current_flightroute =
                        flightRouteDAO.get_flightroute_from_id(FLIGHT_ROUTE_ID);

                if (current_flightroute == null) {
                    current_flightroute = new FlightRoute();
                    current_flightroute.flight_route_id = FLIGHT_ROUTE_ID;
                    current_flightroute.route = route;
                    flightRouteDAO.insertFlightRoute(current_flightroute);
                } else {
                    current_flightroute.route = route;
                    flightRouteDAO.updateFlightRoute(current_flightroute);
                }
            }
        });
    }
}
