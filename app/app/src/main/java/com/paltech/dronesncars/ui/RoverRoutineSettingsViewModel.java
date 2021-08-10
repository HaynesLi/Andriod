package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Rover;

import java.net.InetAddress;
import java.util.List;
import java.util.Timer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * The ViewModel for the {@link RoverRoutineSettingsFragment}, which holds all the LiveData
 * necessary to configure different rovers and their routes. A subclass of {@link ViewModel}.
 */
@HiltViewModel
public class RoverRoutineSettingsViewModel extends ViewModel {

    /**
     * The repository
     */
    private final Repository repository;

    // TODO delete if possible (unused?)
    /**
     * The MutableLiveData representing the number of rovers
     */
    private MutableLiveData<Integer> _num_of_rovers = new MutableLiveData<>();

    /**
     * The LiveData corresponding to {@link #_num_of_rovers}. Value cannot be changed directly.
     */
    public LiveData<Integer> num_of_rovers = _num_of_rovers;

    // TODO delete if possible (unused?)
    /**
     * set the number of rovers in the database to a specific value
     * @param num_of_rovers the number of rover to set the value to
     */
    public void set_num_of_rovers(int num_of_rovers) {
        repository.setNumOfRovers(num_of_rovers, this._num_of_rovers::postValue);
    }

    /**
     * get the number of rovers. As database access is asynchronous, the MutableLiveData-method
     * postValue() is used to update {@link #_num_of_rovers}.
     */
    private void getNumOfRovers() {
        repository.getNumOfRovers(_num_of_rovers::postValue);
    }

    /**
     * Start updating the Rovers continuously. Uses an interval of 10 seconds
     * @return the Timer used to schedule the continuous update.
     */
    public Timer startRoverUpdates(){
        return repository.updateAllRoversContinuously(10, false);
    }

    /**
     * add a rover new rover to the database
     * @param rover_name the rover's name
     * @param inet_address the rover's ip address
     * @param callback the callback used to show an error message via Toast if the rover could not
     *                 be added (e.g. because a rover with that ip address already exists)
     */
    public void add_Rover(String rover_name, InetAddress inet_address, ViewModelCallback<String> callback) {
        repository.create_new_rover(rover_name, inet_address, callback);
    }

    @Inject
    public RoverRoutineSettingsViewModel(Repository repository) {
        this.repository = repository;
        getNumOfRovers();
    }

    /**
     * start the asynchronous computation of rover routes
     */
    public void start_rover_routes_computation() {
        repository.start_rover_routes_computation();
    }

    /**
     * get the LiveData-List of all rovers
     * @return the LiveData-List of all rovers
     */
    public LiveData<List<Rover>> get_all_rovers_livedata() {
        return repository.getCurrentRovers();
    }

    /**
     * delete a rover from the database
     * @param rover the rover to delete
     */
    public void delete_rover(Rover rover) {
        repository.delete_rover(rover);
    }

    /**
     * set a rover as "used" in the database
     * @param rover the rover to modify
     * @param set_used boolean specifying which value to set the rovers {@link Rover#is_used} to
     */
    public void set_rover_used(Rover rover, boolean set_used) {
        repository.set_rover_used(rover, set_used);
    }

    /**
     * get the number of used rovers as livedata
     * @return the LiveData number of used rovers
     */
    public LiveData<Integer> get_num_of_used_rovers() {
        return repository.get_num_of_used_rovers_livedata();
    }

    /**
     * get the number of connected rovers as livedata
     * @return the LiveData number of connected rovers
     */
    public LiveData<Integer> get_num_of_connected_rovers() {
        return repository.get_num_of_connected_rovers_livedata();
    }

    /**
     * get the number if rovers as livedata
     * @return the LiveData number of rovers
     */
    public LiveData<Integer> get_num_of_rovers() {
        return repository.get_num_of_rovers_livedata();
    }

    /**
     * associate rovers to the computed routes
     * @param callback_for_toast the callback used to display an error message to the user using
     *                           Toast (if necessary)
     */
    public void associate_rovers_to_routes(ViewModelCallback<String> callback_for_toast) {
        repository.associate_rovers_to_routes(callback_for_toast);
    }
}
