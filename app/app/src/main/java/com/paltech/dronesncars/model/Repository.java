package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.paltech.dronesncars.computing.FlightRouteGenerator;
import com.paltech.dronesncars.computing.VRP_Wrapper;
import com.paltech.dronesncars.computing.WeedDetectorInterface;
import com.paltech.dronesncars.computing.WeedDetectorMock;
import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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

    private final StorageManager storageManager;


    private final int POLYGON_ID = 1;
    private final int ROUTINE_ID = 1;
    private final int DRONE_SETTING_ID = 1;
    private final int FLIGHT_ROUTE_ID = 1;


    @Inject
    public Repository(@ApplicationContext Context context, Executor executor, StorageManager storageManager) {
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
        this.storageManager = storageManager;
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

                set_flight_route(route);
            }
        });
    }

    public void set_flight_route(List<GeoPoint> route) {
        executor.execute(() -> {
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
        });
    }

    public void save_kml_doc_from_polygon(Polygon polygon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            KmlDocument kmlDocument = KMLParser.polygon_to_kml(polygon);

            StringWriter string_writer = new StringWriter();
            kmlDocument.saveAsKML(string_writer);
            String kml_string = string_writer.toString();

            try {
                string_writer.close();
            } catch (IOException io_exception) {
                io_exception.printStackTrace();
                Log.d("DEV_ERROR", "Failed to close StringWriter while saving Polygon to KML");
            }

            // TODO maybe use the actual time zone here?
            SimpleDateFormat simple_date_format = new SimpleDateFormat("HH-mm_dd-MM-yyyy", Locale.GERMANY);
            String now = simple_date_format.format(new Date());
            String file_name = now + "_saved_polygon.kml";

            storageManager.save_string_to_file(file_name, kml_string);

        }
    }

    public LiveData<List<Result>> get_scan_results() {
        return resultDAO.get_all_results_livedata();
    }

    public LiveData<List<RoverRoute>> get_rover_routes() {
        return roverRouteDAO.get_all_rover_routes_livedata();
    }

    public void mock_results() {

        executor.execute(() -> {
            Polygon current_polygon = polygonModelDAO.getPolygonModelByID(POLYGON_ID).polygon;
            if (current_polygon != null) {
                WeedDetectorInterface weed_detector = new WeedDetectorMock(current_polygon);
                List<Result> mock_results = weed_detector.get_results_from_pictures(
                        new ArrayList<>(),
                        new ArrayList<>()
                );

                if (!mock_results.isEmpty()) {
                    resultDAO.delete_all_results();
                    Result[] result_array = new Result[mock_results.size()];
                    resultDAO.insertMultipleResults(mock_results.toArray(result_array));
                }
            }
        });
    }

    public void start_rover_routes_computation(int num_of_rovers) {
        executor.execute(() -> {
            List<Result> current_results = resultDAO.getAllResults();
            List<GeoPoint> targets = current_results.stream().map(result -> result.location).collect(Collectors.toList());

            if(!targets.isEmpty() && num_of_rovers > 0) {
                List<List<GeoPoint>> routes = VRP_Wrapper.get_routes_for_vehicles(num_of_rovers, targets);

                RoverRoutine rover_routine;
                int error_counter = 0;
                do {
                    rover_routine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
                    if (rover_routine == null) {
                        error_counter++;
                        try {
                            // TODO: does this work like this?
                            wait(5000);
                        } catch (InterruptedException e) {
                            Log.e("ERROR_WAITING", "start_rover_routes_computation: interrupt while waiting - ", e);
                        }
                    }
                } while(rover_routine == null && error_counter < 4);

                if (error_counter >= 4) {
                    return;
                }

                for (int rover_route_id = 0; rover_route_id < routes.size(); rover_route_id++) {
                    List<GeoPoint> current_route = routes.get(rover_route_id);
                    RoverRoute new_rover_route = new RoverRoute(rover_route_id, -1, current_route, ROUTINE_ID);

                    roverRouteDAO.insertMultiple(new_rover_route);
                }
            }
        });
    }
}
