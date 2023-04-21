package com.paltech.dronesncars.ui;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.FlightRoute;
import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverRoute;
import com.paltech.dronesncars.model.RoverRoutine;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.Dictionary;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * The ViewModel used by all 3 Map-Fragments: {@link MapFragment}, {@link FlightMapFragment} and
 * {@link RoverMap}. It holds all the LiveData necessary to display and change polygons, flight and
 * rover routes. Is a subclass of {@link ViewModel}
 */
@HiltViewModel
public class MapViewModel extends ViewModel {
    public final int map_Id = 1;

    /**
     * The repository
     */
    private final Repository repository;

    /**
     * The private MutableLiveData which represents the polygon. Can be used to change the polygon
     */
    private final MutableLiveData<Polygon> _polygon = new MutableLiveData<>();

    /**
     * The corresponding LiveData to {@link #_polygon}. Value cannot be changed directly.
     */
    public LiveData<Polygon> polygon = _polygon;

    /**
     * get the LiveData for the flight-route
     * @return the LiveData for an object of type {@link FlightRoute}
     */
    public LiveData<FlightRoute> getRoute() {
        return this.repository.get_current_flightroute();
    }

    /**
     * the MutableLiveData which is used to represent all Polygons originating in one KML file which
     * can be chosen by the user. Changeable
     */
    private final MutableLiveData<Dictionary<String, Polygon>> _choosePolygonFromKML = new MutableLiveData<>();

    /**
     * The LiveData correspondiong to {@link #_choosePolygonFromKML}.
     * Value cannot be changed directly.
     */
    public LiveData<Dictionary<String, Polygon>> choosePolygonFromKML = _choosePolygonFromKML;

    /**
     * Trigger the parsing of a kml file. the parsing is an asynchronous process and the
     * postValue()-method of MutableLiveData is used to update {@link #_choosePolygonFromKML}
     * accordingly.
     * @param kml_file_uri the Uri of the KML-File to parse
     */
    public void parseKMLFile(Uri kml_file_uri) {
        repository.parseKMLFile(kml_file_uri, _choosePolygonFromKML::postValue);
    }

    /**
     * delete all selectable polygons from the database
     */
    public void clearSelectablePolygons() {
        repository.clearSelectablePolygons(_choosePolygonFromKML::setValue);
    }

    /**
     * set the polygon in the database
     * @param polygon the polygon to insert into the database
     */
    public void setPolygon(Polygon polygon) {
        if (polygon != null) {
            if (polygon.getActualPoints() != null && !polygon.getActualPoints().isEmpty())
            repository.setPolygon(polygon, _polygon::postValue);
        }
    }

    /**
     * get the polygon in the database, but asynchronous. uses MutableLiveData-method postValue() to
     * store the result in {@link #_polygon}.
     */
    public void getPolygon() {
        repository.getPolygon(_polygon::postValue);
    }

    /**
     * delete the polygon from the database. there will be a short-time inconsistency between
     * {@link #_polygon} and the database until the database catches up due to postValue()
     */
    public void clearPolygon() {
        _polygon.setValue(null);
        repository.clearPolygon(_polygon::postValue);
    }

    /**
     * set the flight route in the database
     * @param route the flight route to insert into the database
     */
    public void set_flight_route(List<GeoPoint> route) {
        repository.set_flight_route(route);
    }

    /**
     * set the rover routes in the database
     * @param routes the routes
     * @param is_navigation_point_lists the corresponding list of booleans, which determines for
     *                                  every waypoint inside the routes whether it is a only there
     *                                  for navigational purposes or not
     */
    public void set_rover_routes(List<List<GeoPoint>> routes, List<List<Boolean>> is_navigation_point_lists) {
        repository.set_rover_routes(routes, is_navigation_point_lists);
    }

    @Inject
    public MapViewModel(Repository repository) {
        this.repository = repository;
        _choosePolygonFromKML.setValue(this.repository.getPolygonsToChoose());
        getPolygon();
    }

    /**
     * save the current polygon to a kml file
     */
    public void save_polygon_to_kml() {
        if (polygon != null && polygon.getValue() != null) {
            repository.save_kml_doc_from_polygon(polygon.getValue());
        }
    }

    /**
     * get the rover routes currently in the database, which were computed most recently.
     * @return the Livedata List of these Rover-Routes
     */
    public LiveData<List<RoverRoute>> get_rover_routes() { return this.repository.get_current_rover_routes();}

    /**
     * A MediatorLiveData representing the in {@link RoverStatusFragment} currently observed rover.
     * Using the MediatorLiveData allows us to change the currently observed rover while still
     * listening to small changes in the currently observed rover itself.
     */
    MediatorLiveData<Rover> status_observed_rover = new MediatorLiveData<>();

    /**
     * The LiveData we currently use as source for the {@link #status_observed_rover}.
     */
    private LiveData<Rover> observed_rover_source = null;

    /**
     * change the Source of the {@link #status_observed_rover} to the clicked rover
     * @param clicked_rover the clicked rover
     */
    public void set_status_observed_rover(Rover clicked_rover) {
        if (observed_rover_source != null) {
            status_observed_rover.removeSource(observed_rover_source);
            if (clicked_rover.equals(observed_rover_source.getValue())) {
                status_observed_rover.setValue(null);
                observed_rover_source = null;
                return;
            }
        }
        observed_rover_source = repository.get_livedata_observed_rover(clicked_rover);
        this.status_observed_rover.setValue(null);
        this.status_observed_rover.addSource(observed_rover_source, value -> status_observed_rover.setValue(value));
    }

    /**
     * get LiveData for the current rover routine (e.g. the currently used rover routes out of all
     * the routes saved in the database)
     * @return the LiveData for the RoverRoutine
     */
    public LiveData<RoverRoutine> get_rover_routine_livedata() {
        return repository.get_rover_routine_livedata();
    }
}
