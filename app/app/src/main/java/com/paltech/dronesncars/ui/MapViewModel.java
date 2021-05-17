package com.paltech.dronesncars.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Map;

public class MapViewModel extends ViewModel {
    public int map_Id;

    public MutableLiveData<Map> getMap() {
        return map;
    }

    public void setMap(MutableLiveData<Map> map) {
        this.map = map;
    }

    private MutableLiveData<Map> map = new MutableLiveData<>();



}
