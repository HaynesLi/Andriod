package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;

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

    @Inject
    public RoverRoutineSettingsViewModel(Repository repository) {
        this.repository = repository;
        getNumOfRovers();
    }
}
