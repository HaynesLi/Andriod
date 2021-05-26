package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RoverStatusViewModel extends ViewModel {

    private final Repository repository;

    public LiveData<List<Rover>> getAllRovers() {
        return this.repository.getCurrentRovers();
    }

    @Inject
    public RoverStatusViewModel(Repository repository) {
        this.repository = repository;
    }

}
