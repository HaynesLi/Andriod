package com.paltech.dronesncars.model;

import android.net.InetAddresses;
import android.net.Uri;
import android.os.FileUtils;
import android.util.Log;

import com.paltech.dronesncars.ui.RoverUpdateModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public class RoverConnection {

    private Repository repository;
    private Executor executor;
    private boolean wasCalledInStatusFragment;

    public RoverConnection(Repository repository, Executor executor){
        this.repository = repository;
        this.executor = executor;
    }

    public Timer updateAllRoversContinuously(int secondsBetweenUpdate, boolean wasCalledInStatusFragment){
        this.wasCalledInStatusFragment = wasCalledInStatusFragment;
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
        IGetJson requestClient = retrofit.create(IGetJson.class);
        requestClient.getJSON().enqueue(new Callback<RoverUpdateModel>() {
            @Override
            public void onResponse(Call<RoverUpdateModel> call, Response<RoverUpdateModel> response) {
                if(response.isSuccessful()){
                    rover.battery = response.body().getBattery();
                    rover.position = new GeoPoint(response.body().getLatitude(), response.body().getLongitude());
                    rover.mission = response.body().getMission();
                    rover.status = RoverStatus.CONNECTED;
                    if(rover.waypoints != null && rover.waypoints.size() != 0) {
                        if (wasCalledInStatusFragment) {
                            int current_waypoint = response.body().getCurrentWaypoint();
                            for (; rover.currentWaypoint < current_waypoint; rover.currentWaypoint++) {
                                if (!rover.waypoints.get(rover.currentWaypoint).is_navigation_point) {
                                    getWaypointPicPrevious(rover.ip_address, rover.mission, rover.currentWaypoint + 1);
                                    getWaypointPicAfter(rover.ip_address, rover.mission, rover.currentWaypoint + 1);
                                    getWaypointInfo(rover.ip_address, rover.mission, rover.currentWaypoint + 1);
                                }
                                rover.waypoints.get(rover.currentWaypoint).milestone_completed = true;
                            }
                        }
                        rover.progress = (double) rover.currentWaypoint / (double) rover.waypoints.size();
                    }
                }else{
                    rover.status = RoverStatus.DISCONNECTED;
                    if(!wasCalledInStatusFragment) {
                        rover.is_used = false;
                    }
                }
                executor.execute(()-> repository.updateRover(rover));
            }

            @Override
            public void onFailure(Call<RoverUpdateModel> call, Throwable t) {
                rover.status = RoverStatus.DISCONNECTED;
                if(!wasCalledInStatusFragment) {
                    rover.is_used = false;
                }
                executor.execute(()-> repository.updateRover(rover));
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    public void getWaypointPicPrevious(InetAddress ip_address, String mission, int waypoint){
        final String BASE_URL = "http:/"+ip_address.getHostAddress() + ":5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IGetPreviousPic requestClient = retrofit.create(IGetPreviousPic.class);
        requestClient.getPreviousPic(mission, waypoint).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    writeResponseBodyToDisk(mission,waypoint,"previous.jpeg", response.body());
                }else{
                    Log.d("RoverConnection", "Response was not successful");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    public void getWaypointPicAfter(InetAddress ip_address, String mission, int waypoint){
        final String BASE_URL = "http:/"+ip_address.getHostAddress() + ":5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IGetAfterPic requestClient = retrofit.create(IGetAfterPic.class);
        requestClient.getAfterPic(mission, waypoint).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    writeResponseBodyToDisk(mission,waypoint,"after.jpeg", response.body());             //writeResponseBodyToDisk(path, response.body());
                }else{
                    Log.d("RoverConnection", "Response was not successful");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    public void getWaypointInfo(InetAddress ip_address, String mission, int waypoint){
        final String BASE_URL = "http:/"+ip_address.getHostAddress() + ":5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        IGetWaypointInfo requestClient = retrofit.create(IGetWaypointInfo.class);
        requestClient.getWaypointInfo(mission, waypoint).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    writeResponseBodyToDisk(mission,waypoint,"waypoint_data.json", response.body());                  //writeResponseBodyToDisk(path, response.body());
                }else{
                    Log.d("RoverConnection", "Response was not successful");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    public interface IGetJson {
        @GET("/getJSON")
        Call<RoverUpdateModel> getJSON();
    }

    public interface IGetPreviousPic {
        @GET("/getWaypointPicPrevious/{missionId}/{waypointNumber}")
        Call<ResponseBody> getPreviousPic(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }
    public interface IGetAfterPic {
        @GET("/getWaypointPicAfter/{missionId}/{waypointNumber}")
        Call<ResponseBody> getAfterPic(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }
    public interface IGetWaypointInfo {
        @GET("/getWaypointInfo/{missionId}/{waypointNumber}")
        Call<ResponseBody> getWaypointInfo(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }

    public void uploadMissionFile(Rover rover, List<GeoPoint> list) {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                JSONObject geopoint = new JSONObject();
                geopoint.put("latitude", list.get(i).getLatitude());
                geopoint.put("longitude", list.get(i).getLongitude());
                jsonArray.put(geopoint);
            }
        }catch (JSONException e){
            Log.d("RoverConnection", "Error creating the mission.json File: "+e.getMessage());
        }
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        String path = repository.getContext().getFilesDir()+"/mission_"+rover.rover_id+".json";
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        }catch(IOException e){
            Log.d("RoverConnection", "Write to File error");
        }
        // create upload service client
        final String BASE_URL = "http:/"+rover.ip_address.getHostAddress() + ":5000";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FileUploadService service = retrofit.create(FileUploadService.class);

        // create RequestBody instance from file
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody requestFile = RequestBody.create(JSON ,file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("mission", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "description";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        // finally, execute the request
        Call<ResponseBody> call = service.upload(description, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Log.d("RoverConnection", "Upload of Mission for Rover("+rover.rover_id+") success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "Upload Error: "+t.getMessage());
            }
        });
    }

    public interface FileUploadService {
        @Multipart
        @POST("/uploadMission")
        Call<ResponseBody> upload(
                @Part("description") RequestBody description,
                @Part MultipartBody.Part file
        );
    }

    private void checkDirectories(String mission, int waypoint){
        String base_path = repository.getContext().getFilesDir()+"/Milestones/Mission_"+mission+"/Waypoint_"+waypoint;
        File file = new File(base_path);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    private boolean writeResponseBodyToDisk(String mission, int waypoint, String filename,ResponseBody body) {
        try {
            checkDirectories(mission, waypoint);
            String path = repository.getContext().getFilesDir()+"/Milestones/Mission_"+mission+"/Waypoint_"+waypoint+"/"+filename;
            // todo change the file location/name according to your need
            File file = new File(path);


            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
