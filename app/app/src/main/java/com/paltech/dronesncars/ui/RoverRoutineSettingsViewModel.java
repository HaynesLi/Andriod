package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;

import java.util.ArrayList;
import java.util.List;

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

    public void add_rovers(int num_of_rovers) {
        ArrayList<Rover> rovers = new ArrayList<>();
        for (int i = 0; i < num_of_rovers; i++) {
            rovers.add(new Rover(i, "Hubert_"+i, -1.0));
        }

        repository.setCurrentRovers(rovers);
    }

    @Inject
    public RoverRoutineSettingsViewModel(Repository repository) {
        this.repository = repository;
        getNumOfRovers();
    }

    public void start_rover_routes_computation(int num_of_rovers) {
        repository.start_rover_routes_computation(num_of_rovers);
    }

    public LiveData<List<Rover>> get_all_rovers_livedata() {
        return repository.getCurrentRovers();
    }

    public void delete_rover(Rover rover) {
        repository.delete_rover(rover);
    }
}
