package com.paltech.dronesncars.model;

import android.util.Log;

import com.paltech.dronesncars.ui.RoverUpdateModel;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RoverConnection {

    private Repository repository;
    private Executor executor;

    public RoverConnection(Repository repository, Executor executor){
        this.repository = repository;
        this.executor = executor;
    }

    public Timer updateAllRoversContinuously(int secondsBetweenUpdate){
        int delay = 0;
        int period = secondsBetweenUpdate*1000;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateAllRovers();
            }
        }, delay, period);
        return timer;
    }

    public void updateAllRovers(){
        executor.execute(()->{
            final List<Rover> rovers  = repository.getRovers();
            for(int i = 0; i< rovers.size(); i++){
                updateRover(rovers.get(i));
            }
        });
    }

    public void updateRover(Rover rover){
        final String BASE_URL = "http:/"+rover.ip_address.getHostAddress() + ":5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IPlusService requestClient = retrofit.create(IPlusService.class);
        requestClient.getJSON().enqueue(new Callback<RoverUpdateModel>() {
            @Override
            public void onResponse(Call<RoverUpdateModel> call, Response<RoverUpdateModel> response) {
                if(response.isSuccessful()){
                    rover.battery = response.body().getBattery();
                    rover.position = response.body().getPosition();
                    rover.currentWaypoint = response.body().getCurrentWaypoint();
                    rover.mission = response.body().getMission();
                    rover.status = RoverStatus.CONNECTED;
                }else{
                    Log.d("RoverConnection", "onFailure: Response but failed");
                    rover.status = RoverStatus.DISCONNECTED;
                }
                executor.execute(()->{
                    repository.updateRover(rover);
                });
            }

            @Override
            public void onFailure(Call<RoverUpdateModel> call, Throwable t) {
                rover.status = RoverStatus.DISCONNECTED;
                Log.d("RoverConnection", "onFailure: FAILED");
                executor.execute(()->{
                    repository.updateRover(rover);
                });
            }
        });
    }

    public interface IPlusService {
        @GET("/getJSON")
        Call<RoverUpdateModel> getJSON();
    }
}
