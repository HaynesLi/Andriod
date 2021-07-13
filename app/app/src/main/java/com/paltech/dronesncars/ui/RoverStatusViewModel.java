package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;

import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RoverStatusViewModel extends ViewModel {

    private final Repository repository;

    public LiveData<List<Rover>> getAllRovers() {
        return this.repository.getCurrentRovers();
    }

    public LiveData<List<Rover>> getUsedRovers() { return this.repository.getUsedRovers(); }

    public Timer startRoverUpdates(){
        return repository.updateAllRoversContinuously(10);
    }

    @Inject
    public RoverStatusViewModel(Repository repository) {
        this.repository = repository;
    }

    public void mock_progress_update() {
        repository.mock_progress_update();
    }
}
