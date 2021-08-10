package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;

import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * The ViewModel used by {@link RoverStatusFragment}, which holds all the LiveData necessary to
 * display and update different info regarding the status of used rovers. A subclass of
 * {@link ViewModel}.
 */
@HiltViewModel
public class RoverStatusViewModel extends ViewModel {

    /**
     * The repository
     */
    private final Repository repository;

    /**
     * get all rovers as livedata
     * @return list of rovers as livedata
     */
    public LiveData<List<Rover>> getAllRovers() {
        return this.repository.getCurrentRovers();
    }

    /**
     * get all used rovers as livedata
     * @return list of used rovers as livedata
     */
    public LiveData<List<Rover>> getUsedRovers() { return this.repository.getUsedRovers(); }

    /**
     * start the continuous updating of rover information (every 10 seconds).
     * @return the Timer used to schedule the updates
     */
    public Timer startRoverUpdates(){
        return repository.updateAllRoversContinuously(10, true);
    }

    @Inject
    public RoverStatusViewModel(Repository repository) {
        this.repository = repository;
    }

    // TODO delete if we do not need this anymore? (Check for usage before deleting)

    /**
     * mock a progress update in the first used and displayed rover. The result will be stored
     * directly in the database (asynchronous).
     */
    public void mock_progress_update() {
        repository.mock_progress_update();
    }
}
