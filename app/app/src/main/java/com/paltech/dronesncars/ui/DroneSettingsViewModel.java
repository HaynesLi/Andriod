package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DroneSettingsViewModel extends ViewModel {

    private final Repository repository;

    private final MutableLiveData<Integer> _flight_altitude = new MutableLiveData<>();
    public LiveData<Integer> flight_altitude = _flight_altitude;

    @Inject
    public DroneSettingsViewModel(Repository repository) {
        this.repository = repository;
        getFlightAltitude();
    }

    public void setFlightAltitude(int flight_altitude) {
        repository.setFlightAltitude(flight_altitude, this._flight_altitude::postValue);
    }

    private void getFlightAltitude() {
        repository.getFlightAltitude(_flight_altitude::postValue);
    }
    public void computeRoute() {
        repository.computeRoute();
    }
}
