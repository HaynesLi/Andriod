package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.paltech.dronesncars.computing.FlightRouteGenerator;
import com.paltech.dronesncars.computing.VRP_Wrapper;
import com.paltech.dronesncars.computing.WeedDetectorInterface;
import com.paltech.dronesncars.computing.WeedDetectorMock;
import com.paltech.dronesncars.ui.RoverUpdateModel;
import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

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
    private final RoverConnection roverConnection;
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
        roverConnection = new RoverConnection(this, executor);
    }

    public void updateRover(Rover rover){
        roverDAO.update(rover);
    }

    public Timer updateAllRoversContinuously(int secondsBetweenUpdate, boolean wasCalledInStatusFragment){
       return roverConnection.updateAllRoversContinuously(secondsBetweenUpdate, wasCalledInStatusFragment);
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

    public LiveData<List<Rover>> getUsedRovers(){
        return roverDAO.getUsedRoversLiveData();
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
            PolygonModel current_polygon_model = polygonModelDAO.getPolygonModelByID(POLYGON_ID);
            if (current_polygon_model != null) {
                Polygon current_polygon = current_polygon_model.polygon;
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
            }
        });
    }

    private void create_rover_routine() {
        RoverRoutine rover_routine = new RoverRoutine(ROUTINE_ID);
        roverRoutineDAO.insert(rover_routine);
    }

    public void start_rover_routes_computation() {
        executor.execute(() -> {
            List<Result> current_results = resultDAO.getAllResults();
            List<GeoPoint> targets = current_results.stream().map(result -> result.location).collect(Collectors.toList());
            int num_of_rovers = roverDAO.get_num_of_used_rovers();

            if(check_for_missing_rover_routine()) create_rover_routine();

            if(!targets.isEmpty() && num_of_rovers > 0) {
                List<List<GeoPoint>> routes = VRP_Wrapper.get_routes_for_vehicles(num_of_rovers, targets);

                if (!routes.isEmpty()) {
                    roverRouteDAO.delete_rover_routes_by_routine_id(ROUTINE_ID);
                }

                insert_rover_routes(routes);
            }
        });
    }

    private boolean check_for_missing_rover_routine() {
        RoverRoutine rover_routine;
        rover_routine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
        return rover_routine == null;
    }

    // TODO maybe add the copying of corresponding rover id from old routes to new routes?
    public void set_rover_routes(@NonNull List<List<GeoPoint>> rover_routes) {
        executor.execute(() -> {

            if(check_for_missing_rover_routine()) create_rover_routine();

            List<RoverRoute> current_rover_routes = roverRouteDAO.getAllRoverRoutes();
            if(current_rover_routes != null && !current_rover_routes.isEmpty() && !rover_routes.isEmpty()) {
                roverRouteDAO.delete_rover_routes_by_routine_id(ROUTINE_ID);
            }

            insert_rover_routes(rover_routes);
        });
    }

    private void insert_rover_routes(List<List<GeoPoint>> rover_routes) {
        for (int rover_route_id = 0; rover_route_id < rover_routes.size(); rover_route_id++) {
            List<GeoPoint> current_route = rover_routes.get(rover_route_id);
            RoverRoute new_rover_route = new RoverRoute(rover_route_id, -1, current_route, ROUTINE_ID);

            roverRouteDAO.insertMultiple(new_rover_route);
        }
    }

    public void delete_rover(Rover rover) {
        executor.execute(() -> roverDAO.delete(rover));
    }

    public void set_rover_used(Rover rover, boolean set_used) {
        executor.execute(() -> {
            Rover current_rover = roverDAO.getRoverByID(rover.rover_id);
            if (current_rover == null) {
                current_rover = rover;
                current_rover.is_used = set_used;
                roverDAO.insertMultipleRovers(current_rover);
            } else {
                current_rover.is_used = set_used;
                roverDAO.update(current_rover);
            }
        });
    }

    public void create_new_rover(String roverName, InetAddress ip_address, ViewModelCallback<String> callback ){
        executor.execute(()->{
            final List<Integer> roverIds = roverDAO.get_all_ids_not_livedata();
            final List<InetAddress> ip_adresses= roverDAO.get_all_ip_addresses_not_livedata();
            if(!ip_adresses.contains(ip_address)) {
                //if(roverIds.getValue() != null) {
                for (int i = 0; i < roverIds.size() + 1; i++) {
                    if (!roverIds.contains(i)) {
                        Rover rover = new Rover(i, ip_address);
                        rover.roverName = roverName;
                        rover.ip_address = ip_address;
                        roverDAO.insertMultipleRovers(rover);
                        break;
                    }
                }
                //}
            }else{
                callback.onComplete(ip_address.getHostAddress()+" is already used for rover with name: "+roverDAO.getRoverByIpAddress(ip_address).roverName);
            }
        });
    }

    public LiveData<Integer> get_num_of_used_rovers_livedata() {
        return roverDAO.get_num_of_used_rovers_livedata();
    }

    public LiveData<Integer> get_num_of_connected_rovers_livedata() {
        return roverDAO.get_num_connected_rovers_livedata();
    }

    public LiveData<Integer> get_num_of_rovers_livedata() {
        return roverDAO.get_num_rovers_livedata();
    }

    public void associate_rovers_to_routes(ViewModelCallback<String> callback_for_toast) {
        executor.execute(() -> {
            List<Rover> all_rovers = roverDAO.getAll();
            List<RoverRoute> all_rover_routes = roverRouteDAO.getAllRoverRoutes();

            int index_of_todo_routes = 0;
            int index_of_next_rover = 0;
            while(index_of_todo_routes < all_rover_routes.size() && index_of_next_rover < all_rovers.size()) {
                RoverRoute current_route = all_rover_routes.get(index_of_todo_routes);
                Rover current_rover = all_rovers.get(index_of_next_rover);

                if (current_rover.is_used && current_rover.status == RoverStatus.CONNECTED) {
                    current_route.corresponding_rover_id = current_rover.rover_id;
                    roverRouteDAO.update(current_route);
                    index_of_todo_routes++;
                } else if (current_rover.is_used) {
                    current_rover.is_used = false;
                    roverDAO.update(current_rover);
                }
                index_of_next_rover++;
            }

            if (index_of_todo_routes < all_rover_routes.size()) {
                callback_for_toast.onComplete("Not enough rovers selected & connected! This may result in not every target being reached...");
            }
        });
    }

    public MutableLiveData<Rover> get_livedata_observed_rover(Rover observed_rover) {
        return roverDAO.get_rover_by_id_mutable_livedata(observed_rover.rover_id);
    }
}
