package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverConnection;

import java.net.InetAddress;
import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RoverRoutineSettingsViewModel extends ViewModel {

    private final Repository repository;

    private MutableLiveData<Integer> _num_of_rovers = new MutableLiveData<>();
    public LiveData<Integer> num_of_rovers = _num_of_rovers;

    public void set_num_of_rovers(int num_of_rovers) {
        repository.setNumOfRovers(num_of_rovers, this._num_of_rovers::postValue);
    }

    private void getNumOfRovers() {
        repository.getNumOfRovers(_num_of_rovers::postValue);
    }

    public Timer startRoverUpdates(){
        return repository.updateAllRoversContinuously(10, false);
    }

    public void add_Rover(String rover_name, InetAddress inet_address, ViewModelCallback<String> callback) {
        repository.create_new_rover(rover_name, inet_address, callback);
    }

    @Inject
    public RoverRoutineSettingsViewModel(Repository repository) {
        this.repository = repository;
        getNumOfRovers();
    }

    public void start_rover_routes_computation() {
        repository.start_rover_routes_computation();
    }

    public LiveData<List<Rover>> get_all_rovers_livedata() {
        return repository.getCurrentRovers();
    }

    public void delete_rover(Rover rover) {
        repository.delete_rover(rover);
    }

    public void set_rover_used(Rover rover, boolean set_used) {
        repository.set_rover_used(rover, set_used);
    }

    public LiveData<Integer> get_num_of_used_rovers() {
        return repository.get_num_of_used_rovers_livedata();
    }

    public LiveData<Integer> get_num_of_connected_rovers() {
        return repository.get_num_of_connected_rovers_livedata();
    }

    public LiveData<Integer> get_num_of_rovers() {
        return repository.get_num_of_rovers_livedata();
    }

    public void associate_rovers_to_routes(ViewModelCallback<String> callback_for_toast) {
        repository.associate_rovers_to_routes(callback_for_toast);
    }
}
