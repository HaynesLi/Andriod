package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * The ViewModel holding the LiveData-Sources for the {@link DroneSettingsFragment}. Subclass of
 * {@link ViewModel}
 */
@HiltViewModel
public class DroneSettingsViewModel extends ViewModel {

    /**
     * The repository
     */
    private final Repository repository;

    /**
     * The private MutableLiveData that is used to change the value of the drone flight altitude
     */
    private final MutableLiveData<Integer> _flight_altitude = new MutableLiveData<>();

    /**
     * The LiveData that is actually observed by the the Fragment (its not possible to directly
     * change its value, which results in better separation)
     */
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

    /**
     * triggers the flight route computation in repository. The computation is asynchronous and the
     * result will be saved directly into the database and used from there, hence no return value.
     */
    public void computeRoute() {
        repository.compute_FlightRoute();
    }
}
