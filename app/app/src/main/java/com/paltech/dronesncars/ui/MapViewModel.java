package com.paltech.dronesncars.ui;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;

import org.osmdroid.views.overlay.Polygon;

import java.util.Dictionary;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MapViewModel extends ViewModel {
    public final int map_Id = 1;

    private final Repository repository;

    private final MutableLiveData<Polygon> _polygon = new MutableLiveData<>();
    public LiveData<Polygon> polygon = _polygon;

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

    @Inject
    public MapViewModel(Repository repository) {
        this.repository = repository;
        _choosePolygonFromKML.setValue(this.repository.getPolygonsToChoose());
        getPolygon();
    }

}
