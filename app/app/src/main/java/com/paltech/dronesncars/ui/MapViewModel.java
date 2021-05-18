package com.paltech.dronesncars.ui;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Map;
import com.paltech.dronesncars.model.Repository;

import org.osmdroid.views.overlay.Polygon;

import java.util.Dictionary;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MapViewModel extends ViewModel {
    public final int map_Id = 1;

    private final Repository repository;

    private final MutableLiveData<Map> _map = new MutableLiveData<>();
    public LiveData<Map> map = _map;

    private final MutableLiveData<Dictionary<String, Polygon>> _choosePolygonFromKML = new MutableLiveData<>();
    public LiveData<Dictionary<String, Polygon>> choosePolygonFromKML = _choosePolygonFromKML;

    public void parseKMLFile(Uri kml_file_uri) {
        repository.parseKMLFile(kml_file_uri, _choosePolygonFromKML::postValue);
    }

    public void clearSelectablePolygons() {
        repository.clearSelectablePolygons(_choosePolygonFromKML::setValue);
    }

    @Inject
    public MapViewModel(Repository repository) {
        _choosePolygonFromKML.setValue(repository.getPolygonsToChoose());
        this.repository = repository;
    }

}
