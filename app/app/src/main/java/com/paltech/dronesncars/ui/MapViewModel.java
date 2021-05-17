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
    public int map_Id;

    private Repository repository;

    public MutableLiveData<Map> getMap() {
        return map;
    }

    private MutableLiveData<Map> map = new MutableLiveData<>();
    private MutableLiveData<Dictionary<String, Polygon>> _choosePolygonFromKML = new MutableLiveData<>();
    public LiveData<Dictionary<String, Polygon>> choosePolygonFromKML = _choosePolygonFromKML;

    public void parseKMLFile(Uri kml_file_uri) {
        repository.parseKMLFile(kml_file_uri, result -> _choosePolygonFromKML.postValue(result));
    }

    @Inject
    public MapViewModel(Repository repository) {
        _choosePolygonFromKML.setValue(repository.getPolygonsToChoose());
        this.repository = repository;
    }

}
