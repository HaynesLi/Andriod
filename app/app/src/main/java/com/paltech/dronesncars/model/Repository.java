package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.paltech.dronesncars.computing.FlightRouteGenerator;
import com.paltech.dronesncars.computing.KMLParser;
import com.paltech.dronesncars.computing.VRP_Wrapper;
import com.paltech.dronesncars.computing.WeedDetectorInterface;
import com.paltech.dronesncars.computing.WeedDetectorMock;
import com.paltech.dronesncars.computing.WeedDetectorMock_Baldham;
import com.paltech.dronesncars.ui.ViewModelCallback;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * The Intersection/Interface between the ViewModels and the Database/Computing stuff. Currently
 * Repository is a bit of a God-Class and it might be wise to split it into different "repositories"
 * for different purposes.
 */
public class Repository {

    private final Context context;

    /**
     * The executor required to do database access on threads other than the Main-UI-Thread
     */
    private final Executor executor;

    /**
     * A dictionary used to save polygons that originate from one kml file and that is used to let
     * the user choose one of the polygons to display.
     */
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


    /**
     * The Repository Constructor
     * @param context context required to get the database
     * @param executor the executor required to allow the usage of background threads
     * @param storageManager the storageManager to used to write polygons into their own kml files
     */
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

    /**
     * update the given rover in the database. Needs to be called from a background-thread!
     * @param rover the rover to update
     */
    public void updateRover(Rover rover){
        roverDAO.update(rover);
    }

    /**
     * begin updating the all rovers continuously
     * @param secondsBetweenUpdate the interval in seconds for the update
     * @param wasCalledInStatusFragment TODO @Paul kein Ahnung wozu das gut ist
     * @return the Timer used to schedule the updates, which can be paused & resumed
     */
    public Timer updateAllRoversContinuously(int secondsBetweenUpdate, boolean wasCalledInStatusFragment){
       return roverConnection.updateAllRoversContinuously(secondsBetweenUpdate, wasCalledInStatusFragment);
    }

    /**
     * get all rovers from the database. Has to be called from a background-thread!
     * @return List of all rovers
     */
    public List<Rover> getRovers() {
        return roverDAO.getAll();
    }

    /**
     * get all results from the database. Has to be called from a background-thread!
     * @return List of all results
     */
    public List<Result> getResults() {
        return resultDAO.getAllResults();
    }

    /**
     * get all RoverRoutes from the database. Has to be called from a background-thread!
     * @return List of all RoverRoutes
     */
    public List<RoverRoute> getRoutes() {
        return roverRouteDAO.getAllRoverRoutes();
    }

    /**
     * get all RoverRoutines from the database. Has to be called from a background-thread!
     * @return List of all RoverRoutines
     */
    public List<RoverRoutine> getRoutines() {
        return roverRoutineDAO.getAllRoverRoutines();
    }

    /**
     * get the current (& only) routine from the database
     * @return the current RoverRoutine
     */
    public RoverRoutine getCurrentRoutine() {
        return roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
    }

    /**
     * In a background-thread: parse the given kml file and call the callback which triggers a the
     * change in the {@link com.paltech.dronesncars.ui.MapViewModel}
     * result is asynchronous, hence there is no return value
     * @param kml_file_uri the Uri of the KML file which has to be parsed
     * @param mapViewModelCallback the callback specifying how to update the corresponding ViewModel
     */
    public void parseKMLFile(Uri kml_file_uri, ViewModelCallback<Dictionary<String, Polygon>> mapViewModelCallback) {
        executor.execute(() -> {
            polygonsToChoose = KMLParser.parseKMLFile(kml_file_uri, context);
            mapViewModelCallback.onComplete(polygonsToChoose);
        });
    }

    /**
     * delete all selectable polygons from {@link #polygonsToChoose}
     * @param mapViewModelCallback the callback used to notify the {@link com.paltech.dronesncars.ui.MapViewModel}
     *                             of the change
     */
    public void clearSelectablePolygons(ViewModelCallback<Dictionary<String, Polygon>> mapViewModelCallback) {
        polygonsToChoose = new Hashtable<>();
        mapViewModelCallback.onComplete(polygonsToChoose);
    }

    /**
     * get the Polygons to choose from
     * @return a Dictionary with FIDs -> Polygons to choose from
     */
    public Dictionary<String, Polygon> getPolygonsToChoose() {
        if (polygonsToChoose == null) {
            polygonsToChoose = new Hashtable<>();
        }
        return polygonsToChoose;
    }

    /**
     * set the polygon in the database (in background thread) and use the given callback to notify
     * the {@link com.paltech.dronesncars.ui.MapViewModel} of the change
     * @param polygon polygon to save in the database
     * @param mapViewModelCallback the callback to notify the
     *        {@link com.paltech.dronesncars.ui.MapViewModel}
     */
    public void setPolygon(Polygon polygon, ViewModelCallback<Polygon> mapViewModelCallback) {
        executor.execute(() -> {
            polygonModelDAO.insertPolygonModel(new PolygonModel(POLYGON_ID, polygon));
            mapViewModelCallback.onComplete(polygon);
        });
    }

    /**
     * asynchronous refreshing of the polygon from the database into the
     * {@link com.paltech.dronesncars.ui.MapViewModel} using the callback
     * @param mapViewModelCallback callback used to notify the {@link com.paltech.dronesncars.ui.MapViewModel}
     */
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

    /**
     * delete the polygon from the database and notify the {@link com.paltech.dronesncars.ui.MapViewModel}
     * @param mapViewModelCallback the callback used to notify the {@link com.paltech.dronesncars.ui.MapViewModel}
     */
    public void clearPolygon(ViewModelCallback<Polygon> mapViewModelCallback) {
        executor.execute(() -> {
            PolygonModel comparable = new PolygonModel(POLYGON_ID, null);
            polygonModelDAO.deletePolygonModel(comparable);
            mapViewModelCallback.onComplete(null);
        });
    }

    /**
     * set the flight altitude in the database and notify the {@link com.paltech.dronesncars.ui.DroneSettingsViewModel}
     * @param altitude the altitude to save in the database
     * @param droneSettingCallback the callback used to notify the {@link com.paltech.dronesncars.ui.DroneSettingsViewModel}
     */
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

    /**
     * asynchronous refresh the flight altitude in the {@link com.paltech.dronesncars.ui.DroneSettingsViewModel}
     * using the callback
     * @param droneSettingCallback callback used to notify the {@link com.paltech.dronesncars.ui.DroneSettingsViewModel}
     */
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

    /**
     * set the number of rovers in the database and notify the {@link com.paltech.dronesncars.ui.RoverRoutineSettingsViewModel}
     * using the callback
     * @param numOfRovers the number of rovers to save in the database
     * @param roverSettingCallback the callback used to notify the {@link com.paltech.dronesncars.ui.RoverRoutineSettingsViewModel}
     */
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

    /**
     * asynchronous refresh the number of rovers in {@link com.paltech.dronesncars.ui.RoverRoutineSettingsViewModel}
     * @param roverSettingCallback the callback used to nofity the {@link com.paltech.dronesncars.ui.RoverRoutineSettingsViewModel}
     */
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

    /**
     * get LiveData of all Rovers currently saved in the database
     * @return LiveData of Rover-List
     */
    public LiveData<List<Rover>> getCurrentRovers() {
        return roverDAO.getAllLiveData();
    }

    /**
     * get LiveData of all used Rovers currently saved in the database
     * @return LiveData of Rover-List
     */
    public LiveData<List<Rover>> getUsedRovers(){
        return roverDAO.getUsedRoversLiveData();
    }

    /**
     * set the List of Rovers in the database to a new list
     * @param currentRovers the list to save in the database
     */
    public void setCurrentRovers(List<Rover> currentRovers) {
        executor.execute(() -> {
            roverDAO.deleteAllRovers();
            Rover[] current_rovers_as_array = new Rover[currentRovers.size()];
            current_rovers_as_array = currentRovers.toArray(current_rovers_as_array);
            roverDAO.insertMultipleRovers(current_rovers_as_array);
        });
    }

    /**
     * save the progress of a rover in the database
     * @param rover the rover to save progress in
     * @param progress the new progress value
     */
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

    /**
     * mcok progress in the first of all rovers in the database
     */
    public void mock_progress_update() {
        executor.execute(() -> {
            List<Rover> current_rovers = roverDAO.getAll();
            if (current_rovers.size() > 0) {
                save_rover_progress(current_rovers.get(0), 0.1);
            }
        });
    }

    /**
     * get LiveData of the current FlightRoute
     * @return the LiveData of FlightRoute
     */
    public LiveData<FlightRoute> get_current_flightroute() {
        return flightRouteDAO.get_flightroute_from_id_livedata(FLIGHT_ROUTE_ID);
    }

    /**
     * compute a new flight route and save it in the database
     */
    public void compute_FlightRoute() {
        executor.execute(() -> {
            PolygonModel polygon_model = polygonModelDAO.getPolygonModelByID(POLYGON_ID);
            DroneSetting currentSetting = droneSettingDAO.getDroneSettingByID(DRONE_SETTING_ID);

            if (polygon_model != null && polygon_model.polygon != null && currentSetting != null) {
                List<GeoPoint> route =
                        FlightRouteGenerator.compute_flight_route(polygon_model.polygon, 10);

                set_flight_route(route);
            }
        });
    }

    /**
     * set the FlightRoute in the database to a new value
     * @param route the new FlightRoute
     */
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

    /**
     * save a polygon into its own KML-File
     * the KML-Files name will be "HH-mm_dd-MM-yyyy_saved_polygon.kml"
     * @param polygon the polygon to save in a KML-File
     */
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
                Log.d("KML_SAVING", "Failed to close StringWriter while saving Polygon to KML");
            }

            // TODO maybe use the actual time zone here?
            SimpleDateFormat simple_date_format = new SimpleDateFormat("HH-mm_dd-MM-yyyy", Locale.GERMANY);
            String now = simple_date_format.format(new Date());
            String file_name = now + "_saved_polygon.kml";

            storageManager.save_string_to_file(file_name, kml_string);

        }
    }

    /**
     * get LiveData of all Results in the Database
     * @return LiveData of all Results in the Database
     */
    public LiveData<List<Result>> get_scan_results() {
        return resultDAO.get_all_results_livedata();
    }

    /**
     * get LiveData of all RoveRoutes in the Database
     * @return LiveData of all RoveRoutes in the Database
     */
    public LiveData<List<RoverRoute>> get_current_rover_routes() {
        return roverRouteDAO.get_all_rover_routes_livedata();
    }

    /**
     * get LiveData of the current (& only) RoverRoutine in the database
     * @return LiveData of the current (& only) RoverRoutine in the database
     */
    public LiveData<RoverRoutine> get_rover_routine_livedata() {
        return roverRoutineDAO.get_rover_routine_by_id_livedata(ROUTINE_ID);
    }

    /**
     * mock results for the computer vision part of this project
     * - WeedDetectorMock_Baldham returns 3 GeoPoints on the Verkehrsuebungsplatz Baldham
     * - WeedDetectorMock returns max(polygon.getActualPoints().size(), 10) random points inside
     * the current polygon
     * and finally save the new results in the database
     */
    public void mock_results() {
        executor.execute(() -> {
            PolygonModel current_polygon_model = polygonModelDAO.getPolygonModelByID(POLYGON_ID);
            if (current_polygon_model != null) {
                Polygon current_polygon = current_polygon_model.polygon;
                if (current_polygon != null) {
                    // replace this Mock with the actual CV thingy or with any Mock.
                    // WeedDetectorMock_Baldham returns a very small route in the middle of the
                    // Verkehrsubungsplatz Baldham
                    // WeedDetectorMock returns max(10, polygon.getActualPoints().size()) random
                    // GeoPoints inside the Polygon
                    WeedDetectorInterface weed_detector = new WeedDetectorMock(current_polygon);
                    //WeedDetectorInterface weed_detector = new WeedDetectorMock_Baldham(current_polygon);
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

    /**
     * create a RoverRoutine in the database
     */
    private void create_rover_routine() {
        RoverRoutine rover_routine = new RoverRoutine(ROUTINE_ID);
        roverRoutineDAO.insert(rover_routine);
    }

    /**
     * asynchronous compute the rover routes and save them into the database
     */
    public void start_rover_routes_computation() {
        executor.execute(() -> {
            List<Result> current_results = resultDAO.getAllResults();
            List<GeoPoint> targets = current_results.stream().map(result -> result.location).collect(Collectors.toList());
            int num_of_rovers = roverDAO.get_num_of_used_rovers();

            if(check_for_missing_rover_routine()) create_rover_routine();

            if(!targets.isEmpty() && num_of_rovers > 0) {
                List<List<GeoPoint>> routes = VRP_Wrapper.get_routes_for_vehicles(num_of_rovers, targets);

                insert_rover_routes(routes, null);
            }
        });
    }


    /**
     * check if we do not have a RoverRoutine in the database yet
     * @return true: we do not have a RoverRoutine in the database, false: else
     */
    private boolean check_for_missing_rover_routine() {
        RoverRoutine rover_routine;
        rover_routine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
        return rover_routine == null;
    }


    /**
     * save a set of new routes for rovers in the database
     * the set will be saved in RoverRoutine as the most up to date
     * @param rover_routes the new rover routes represented by a list of GeoPoints to visit
     * @param is_navigation_point_list a list which allows us to determine whether the a GeoPoint in
     *                                 a route is only there for navigational purposes or if there
     *                                 is some weed to drill out
     */
    public void set_rover_routes(@NonNull List<List<GeoPoint>> rover_routes, List<List<Boolean>> is_navigation_point_list) {
        executor.execute(() -> {

            if(check_for_missing_rover_routine()) create_rover_routine();
            insert_rover_routes(rover_routes, is_navigation_point_list);
        });
    }

    /**
     * save a set of new routes for rovers in the database.
     * The set will be saved in RoverRoutine as the most up to date.
     * Each route will receive an id of "yyyy-MM-dd_HH-mm-ss_" + its index in the rover_routes list,
     * where the date is the time of its insertion into the database.
     * This also means that we overwrite old routes if we compute new routes multiple times per
     * minute.
     * @param rover_routes the new rover routes represented by a list of GeoPoints to visit
     * @param is_navigation_point_list a list which allows us to determine whether the a GeoPoint in
     *                                 a route is only there for navigational purposes or if there
     *                                 is some weed to drill out
     */
    private void insert_rover_routes(List<List<GeoPoint>> rover_routes, List<List<Boolean>> is_navigation_point_list) {
        List<Rover> usable_rovers = roverDAO.getUsedRovers();
        int next_usable_rover = 0;
        DateTimeFormatter dt_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String date_string = dt_formatter.format(LocalDateTime.now());
        if(usable_rovers != null && usable_rovers.size() > 0) {
            List<String> new_rover_route_ids = new ArrayList<>();
            List<RoverRoute> new_rover_routes = new ArrayList<>();
            for (int rover_route_id = 0; rover_route_id < rover_routes.size(); rover_route_id++) {
                List<GeoPoint> current_route = rover_routes.get(rover_route_id);
                RoverRoute new_rover_route = new RoverRoute(date_string + "_" +rover_route_id, -1, current_route, ROUTINE_ID);

                if (is_navigation_point_list != null && is_navigation_point_list.size() == rover_routes.size()) {
                    new_rover_route.is_navigation_point = is_navigation_point_list.get(rover_route_id);
                }

                new_rover_route.corresponding_rover_id = usable_rovers.get(next_usable_rover).rover_id;
                usable_rovers.get(next_usable_rover).mission = new_rover_route.rover_route_id;
                roverDAO.update(usable_rovers.get(next_usable_rover));
                if (next_usable_rover < usable_rovers.size()) {
                    next_usable_rover++;
                } else {
                    break;
                }

                new_rover_route_ids.add(new_rover_route.rover_route_id);
                new_rover_routes.add(new_rover_route);
            }

            //save new computed routes as currently available ones in rover routine
            RoverRoutine roverRoutine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
            if (roverRoutine != null) {
                roverRoutine.rover_route_ids = new_rover_route_ids;
                roverRoutineDAO.update(roverRoutine);
            } else {
                roverRoutine = new RoverRoutine(ROUTINE_ID);
                roverRoutine.rover_route_ids = new_rover_route_ids;
                roverRoutineDAO.insert(roverRoutine);
            }

            for (RoverRoute new_rover_route: new_rover_routes) {
                // this means if we compute new routes multiple times per second we overwrite the older ones from the same second
                if (roverRouteDAO.get_rover_route_by_id(new_rover_route.rover_route_id) == null) {
                    roverRouteDAO.insertMultiple(new_rover_route);
                } else {
                    roverRouteDAO.update(new_rover_route);
                }
            }
        }
    }

    /**
     * delete a rover from the database
     * @param rover the rover to delete
     */
    public void delete_rover(Rover rover) {
        executor.execute(() -> roverDAO.delete(rover));
    }

    /**
     * set a rover used in the database and create the rover in the database if it is not present
     * already
     * @param rover the rover to set used
     * @param set_used the value to set {@link Rover#is_used} to
     */
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

    /**
     * create a new rover and save it into the database
     * @param roverName the rover name
     * @param ip_address the rover's InetAddress
     * @param callback the callback used to display an error message if necessary, e.g. because
     *                 there already exists a rover with the same InetAddress
     */
    public void create_new_rover(String roverName, InetAddress ip_address, ViewModelCallback<String> callback ){
        executor.execute(()->{
            final List<Integer> roverIds = roverDAO.get_all_ids_not_livedata();
            final List<InetAddress> ip_adresses= roverDAO.get_all_ip_addresses_not_livedata();
            if(!ip_adresses.contains(ip_address)) {
                for (int i = 0; i < roverIds.size() + 1; i++) {
                    if (!roverIds.contains(i)) {
                        Rover rover = new Rover(i, ip_address);
                        rover.roverName = roverName;
                        rover.ip_address = ip_address;
                        roverDAO.insertMultipleRovers(rover);
                        break;
                    }
                }
            }else{
                callback.onComplete(ip_address.getHostAddress()+" is already used for rover with name: "+roverDAO.getRoverByIpAddress(ip_address).roverName);
            }
        });
    }

    /**
     * get LiveData of the number of used rovers in the database
     * @return LiveData of the number of used rovers in the database
     */
    public LiveData<Integer> get_num_of_used_rovers_livedata() {
        return roverDAO.get_num_of_used_rovers_livedata();
    }

    /**
     * get LiveData of the number of connected rovers in the database
     * @return LiveData of the number of connected rovers in the database
     */
    public LiveData<Integer> get_num_of_connected_rovers_livedata() {
        return roverDAO.get_num_connected_rovers_livedata();
    }

    /**
     * get LiveData of the number of rovers in the database
     * @return LiveData of the number of rovers in the database
     */
    public LiveData<Integer> get_num_of_rovers_livedata() {
        return roverDAO.get_num_rovers_livedata();
    }

    /**
     * associate rovers with routes they should complete and send them their mission plan.
     * will show an error message to the user if a rover that was supposed to be used for a route
     * is not available anymore (== is disconnected).
     * @param callback_for_toast the callback to show the error message if necessary
     */
    public void associate_rovers_to_routes(ViewModelCallback<String> callback_for_toast) {
        executor.execute(() -> {
            RoverRoutine current_routine = roverRoutineDAO.getRoverRoutineByID(ROUTINE_ID);
            if (current_routine != null) {
                Map<Integer, Rover> used_rovers_dict = roverDAO.getUsedRovers().stream().collect(Collectors.toMap(rover -> rover.rover_id, rover -> rover));
                List<String> current_rover_route_ids = current_routine.rover_route_ids;
                List<RoverRoute> current_rover_routes = current_rover_route_ids.stream().map(roverRouteDAO::get_rover_route_by_id).collect(Collectors.toList());

                for (RoverRoute rover_route : current_rover_routes) {
                    if (used_rovers_dict.containsKey(rover_route.corresponding_rover_id)) {
                        Rover used_rover = used_rovers_dict.get(rover_route.corresponding_rover_id);
                        if (used_rover.status != RoverStatus.CONNECTED) {
                            callback_for_toast.onComplete("A Rover initially destined for a route was not available! Computing new routes is advised.");
                            return;
                        } else {
                            ArrayList<Waypoint> waypoints = new ArrayList<>();
                            for (int i = 0; i < rover_route.route.size(); i++) {
                                waypoints.add(new Waypoint(rover_route.rover_route_id, i + 1, rover_route.route.get(i), rover_route.is_navigation_point.get(i), rover_route.rover_route_id));
                            }
                            used_rover.waypoints = waypoints;
                            used_rover.mission = rover_route.rover_route_id;
                            Log.d("Mission_ID:", rover_route.rover_route_id);
                            used_rover.currentWaypoint = 0;
                            roverConnection.uploadMissionFile(used_rover);
                            roverDAO.update(used_rover);
                        }
                    } else {
                        callback_for_toast.onComplete("A Rover selected for Route Computation is not destined to be used anymore! Computing new routes is advised.");
                        return;
                    }
                }
            }
        });
    }

    /**
     * get LiveData of the currently observed rover in the database
     * @return LiveData of the currently observed rover in the database
     */
    public LiveData<Rover> get_livedata_observed_rover(Rover observed_rover) {
        return roverDAO.get_rover_by_id_mutable_livedata(observed_rover.rover_id);
    }

    public Context getContext(){
        return this.context;
    }
}
