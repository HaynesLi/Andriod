package com.paltech.dronesncars.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlightRouteDAO {

    @Insert
    public void insertFlightRoute(FlightRoute flightRoute);

    @Update
    void updateFlightRoute(FlightRoute flightRoute);

    @Delete
    void deleteFlightRoute(FlightRoute flightRoute);

    @Query("SELECT * FROM flightroute WHERE flight_route_id =(:flight_route_id)")
    FlightRoute get_flightroute_from_id(int flight_route_id);

    @Query("SELECT * FROM flightroute WHERE flight_route_id =(:flight_route_id)")
    LiveData<FlightRoute> get_flightroute_from_id_livedata(int flight_route_id);

    @Query("SELECT * FROM flightroute")
    List<FlightRoute> get_all_flight_routes();

    @Query("SELECT COUNT(*) From flightroute")
    int get_num_of_flight_routes();
}
