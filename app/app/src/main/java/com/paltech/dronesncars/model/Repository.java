package com.paltech.dronesncars.model;

import android.content.Context;

import java.util.List;

public class Repository {

    private final DNR_Database database;
    private final RoverDAO roverDAO;
    private final ResultDAO resultDAO;
    private final DroneSettingDAO droneSettingDAO;
    private final MapDAO mapDAO;
    private final RoverRouteDAO roverRouteDAO;
    private final RoverRoutineDAO roverRoutineDAO;
    private int current_routine_id = -1;

    public Repository(Context context) {
        database = DNR_Database.getInstance(context);
        resultDAO = database.getResultDAO();
        roverDAO = database.getRoverDAO();
        droneSettingDAO = database.getDroneSettingDAO();
        mapDAO = database.getMapDAO();
        roverRouteDAO = database.getRoverRouteDAO();
        roverRoutineDAO = database.getRoverRoutineDAO();
    }

    public List<Rover> getRovers() {
        return roverDAO.getAll();
    }

    public List<Result> getResults() {
        return resultDAO.getAllResults();
    }

    public List<RoverRoute> getRoutes() {
        return roverRouteDAO.getAllRoverRoutes();
    }

    public List<RoverRoutine> getRoutines() {
        return roverRoutineDAO.getAllRoverRoutines();
    }

    public RoverRoutine getCurrentRoutine() {
        if (current_routine_id == -1) {
            return null;
        }
        return roverRoutineDAO.getRoverRoutineByID(current_routine_id);
    }

    // TODO add more stuff here...
}
