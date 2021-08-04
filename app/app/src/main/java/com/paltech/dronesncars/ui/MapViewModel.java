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

@HiltViewModel
public class MapViewModel extends ViewModel {
    public final int map_Id = 1;

    private final Repository repository;

    private final MutableLiveData<Polygon> _polygon = new MutableLiveData<>();
    public LiveData<Polygon> polygon = _polygon;

    public LiveData<FlightRoute> getRoute() {
        return this.repository.get_current_flightroute();
    }

    private final MutableLiveData<Dictionary<String, Polygon>> _choosePolygonFromKML = new MutableLiveData<>();
    public LiveData<Dictionary<String, Polygon>> choosePolygonFromKML = _choosePolygonFromKML;

    public void parseKMLFile(Uri kml_file_uri) {
        repository.parseKMLFile(kml_file_uri, _choosePolygonFromKML::postValue);
    }

    public void clearSelectablePolygons() {
        repository.clearSelectablePolygons(_choosePolygonFromKML::setValue);
    }

    public void setPolygon(Polygon polygon) {
        if (polygon != null) {
            if (polygon.getActualPoints() != null && !polygon.getActualPoints().isEmpty())
            repository.setPolygon(polygon, _polygon::postValue);
        }
    }

    public void getPolygon() {
        repository.getPolygon(_polygon::postValue);
    }

    public void clearPolygon() {
        _polygon.setValue(null);
        repository.clearPolygon(_polygon::postValue);
    }

    public void set_flight_route(List<GeoPoint> route) {
        repository.set_flight_route(route);
    }

    public void set_rover_routes(List<List<GeoPoint>> routes) {
        repository.set_rover_routes(routes);
    }

    @Inject
    public MapViewModel(Repository repository) {
        this.repository = repository;
        _choosePolygonFromKML.setValue(this.repository.getPolygonsToChoose());
        getPolygon();
    }

    public void save_polygon_to_kml() {
        if (polygon != null && polygon.getValue() != null) {
            repository.save_kml_doc_from_polygon(polygon.getValue());
        }
    }

    public LiveData<List<RoverRoute>> get_rover_routes() { return this.repository.get_current_rover_routes();}


    MediatorLiveData<Rover> status_observed_rover = new MediatorLiveData<>();
    private LiveData<Rover> observed_rover_source = null;

    public void set_status_observed_rover(Rover clicked_rover) {
        if (observed_rover_source != null) {
            status_observed_rover.removeSource(observed_rover_source);
            if (observed_rover_source.getValue().equals(clicked_rover)) {
                status_observed_rover.setValue(null);
                observed_rover_source = null;
                return;
            }
        }
        observed_rover_source = repository.get_livedata_observed_rover(clicked_rover);
        this.status_observed_rover.setValue(null);
        this.status_observed_rover.addSource(observed_rover_source, value -> status_observed_rover.setValue(value));
    }

    public LiveData<RoverRoutine> get_rover_routine_livedata() {
        return repository.get_rover_routine_livedata();
    }
}
