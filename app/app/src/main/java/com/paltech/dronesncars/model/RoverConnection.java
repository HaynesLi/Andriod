package com.paltech.dronesncars.model;

import android.util.Log;

import com.paltech.dronesncars.ui.RoverUpdateModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;
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

/**
 * RoverConnection is used for communication with the rovers on the filed.
 * For this purpose the class is implementing multiple different server api calls.
 */
public class RoverConnection {

    private Repository repository;
    private Executor executor;
    private boolean wasCalledInStatusFragment;

    /**
     * The constructor is used to create this class and needs two main sources.
     * @param repository Provides access to the database
     * @param executor Provides the structure for asynchronous threads
     */
    public RoverConnection(Repository repository, Executor executor){
        this.repository = repository;
        this.executor = executor;
    }

    /**
     * Is used to update the information from all rovers on the field in a loop using the {@link #updateAllRovers()} method.
     * @param secondsBetweenUpdate Time used for a single loop
     * @param wasCalledInStatusFragment Boolean value to determine the current fragment.
     *                                  The selected but disconnected rovers are shown in the RoverStatusFragment, but not in the RoverRoutineSettingsFragment.
     *                                  As this method is called first in both fragments the global variable is set here.
     * @return the timer that has to be closed to stop the loop
     */
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

    /**
     * Updates every rover once using the {@link #updateRover(Rover)} method.
     * The list of rovers is observed using the repository and the database.
     */
    public void updateAllRovers(){
        executor.execute(()->{
            final List<Rover> rovers  = repository.getRovers();
            for(int i = 0; i< rovers.size(); i++){
                updateRover(rovers.get(i));
            }
        });
    }

    /**
     * Updates a single rover that is specified as input parameter.
     * Every rover has a rest-server running, providing access to a file "rover_data.json" that is filled with all relevant information about the rover.
     * To access these information this method uses an Api-Call that is implemented on the server.
     * The library for this Api-Call is retrofit.
     * First the required url is created from the IP-address of the rover in the network and the port number.
     * Additionally we need an Interface for the specific Api-Call on the server (IGetJson)
     * Once the call was made the response is processed in a callback.
     * If the call fails, as the server is not running or the rover is out of range of the network the onFailure callback is executed. The status of the rover is set to disconnected and in case the rover was selected for a route computation previously he is now not selected anymore.
     * In case the call is accepted on the server and a response gets send back there are two more possibilities.
     * Either the response is negative, maybe due to a server error or anything else. In this case the same things happen as if the call failed.
     * If the response from the server is successful the rover is updated with the returned values.
     * While being in the RoverStatusFragment the currentWaypoint variable is important. When the rover has finished its work at a waypoint this variable is increased and the information and pictures from this waypoint are downloaded using other Api-Calls
     * @param rover The rover that is going to be updated
     */
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

    /**
     * Downloads the picture that was made before the rover started working on a specific waypoint.
     * Is called once the rover has finished his work on this waypoint.
     * Api-Call to download the file from the server of the rover using the IGetPreviousPic interface.
     * The files on the server are saved at specific places with their paths containing the mission_id and the waypoint number.
     * The response is handled in a callback. If the response is successful the file is saved in the internal app storage using the {@link #writeResponseBodyToDisk(String, int, String, ResponseBody)} method
     * @param ip_address of the rover to access the server via the network
     * @param mission The id of the mission to find the files of the current waypoints
     * @param waypoint The waypoint number to specify the file that should be downloaded
     */
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
                    Log.d("RoverConnection", "Response was not successful: failed to download previous.jpeg");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    /**
     * Downloads the picture that was made after the rover finished working on a specific waypoint.
     * Is called once the rover has finished his work on this waypoint.
     * Api-Call to download the file from the server of the rover using the IGetAfterPic interface.
     * The files on the server are saved at specific places with their paths containing the mission_id and the waypoint number.
     * The response is handled in a callback. If the response is successful the file is saved in the internal app storage using the {@link #writeResponseBodyToDisk(String, int, String, ResponseBody)} method
     * @param ip_address of the rover to access the server via the network
     * @param mission The id of the mission to find the files of the current waypoints
     * @param waypoint The waypoint number to specify the file that should be downloaded
     */
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
                    writeResponseBodyToDisk(mission,waypoint,"after.jpeg", response.body());
                }else{
                    Log.d("RoverConnection", "Response was not successful: failed to download after.jpeg");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    /**
     * Downloads the information (json-file) that is created during the rover is working on a specific waypoint.
     * Is called once the rover has finished his work on this waypoint.
     * Api-Call to download the file from the server of the rover using the IGetWaypointInfo interface.
     * The files on the server are saved at specific places with their paths containing the mission_id and the waypoint number.
     * The response is handled in a callback. If the response is successful the file is saved in the internal app storage using the {@link #writeResponseBodyToDisk(String, int, String, ResponseBody)} method
     * @param ip_address of the rover to access the server via the network
     * @param mission The id of the mission to find the files of the current waypoints
     * @param waypoint The waypoint number to specify the file that should be downloaded
     */
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
                    writeResponseBodyToDisk(mission,waypoint,"waypoint_data.json", response.body());
                }else{
                    Log.d("RoverConnection", "Response was not successful: failed to download waypoint_data.json");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("RoverConnection", "onFailure: "+t.getMessage());
            }
        });
    }

    /**
     * Interface to receive the update file for rover. Required by retrofit for the specific Api-Call
     */
    public interface IGetJson {
        @GET("/getJSON")
        Call<RoverUpdateModel> getJSON();
    }

    /**
     * Interface to receive the Previous-Picture from Waypoint <waypointNumber> on Mission <missionId>. Required by retrofit for the specific Api-Call
     */
    public interface IGetPreviousPic {
        @GET("/getWaypointPicPrevious/{missionId}/{waypointNumber}")
        Call<ResponseBody> getPreviousPic(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }

    /**
     * Interface to receive the After-Picture from Waypoint <waypointNumber> on Mission <missionId>. Required by retrofit for the specific Api-Call
     */
    public interface IGetAfterPic {
        @GET("/getWaypointPicAfter/{missionId}/{waypointNumber}")
        Call<ResponseBody> getAfterPic(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }

    /**
     * Interface to receive the Waypoint-Data-File (JSON) from Waypoint <waypointNumber> on Mission <missionId>. Required by retrofit for the specific Api-Call
     */
    public interface IGetWaypointInfo {
        @GET("/getWaypointInfo/{missionId}/{waypointNumber}")
        Call<ResponseBody> getWaypointInfo(@Path("missionId")String missionId, @Path("waypointNumber")int waypointNumber);
    }

    /**
     * Used to upload the computed mission onto the server of a specific rover.
     * The file is a json object that is created right here containing the mission_id and the latitude and longitude of all waypoints in an array. Additionally there is a flag set to true if the waypoint is just a navigation point.
     * Once the json object was created it is saved in the internal app storage using the rover_id in the filename to not override the mission files of other rovers as we are working with multiple robots.
     * Then this file is uploaded to the base directory of the server on the rover using an implemented Api-Call of the server and the corresponding FileUploadService interface.
     * @param rover The rover object corresponding to the rover on the field that should receive the mission.
     *              Used to get the ip_address for the connection and the waypoint information as well as the mission_id for the mission-file
     */
    public void uploadMissionFile(Rover rover) {
        JSONObject mission_file = new JSONObject();
        try {
            mission_file.put("mission_id", rover.mission);
            JSONArray waypoints = new JSONArray();
            for (int i = 0; i < rover.waypoints.size(); i++) {
                Waypoint waypoint = rover.waypoints.get(i);
                JSONObject geopoint = new JSONObject();
                geopoint.put("latitude", waypoint.position.getLatitude());
                geopoint.put("longitude", waypoint.position.getLongitude());
                if(waypoint.is_navigation_point){
                    geopoint.put("skip_work", true);
                }
                waypoints.put(geopoint);
            }
            mission_file.put("route", waypoints);
        }catch (JSONException e){
            Log.d("RoverConnection", "Error creating the mission.json File: "+e.getMessage());
        }
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        String path = repository.getContext().getFilesDir()+"/mission_"+rover.rover_id+".json";
        File file = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(mission_file.toString().getBytes());
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

    /**
     * Interface to upload a file to the base directory of the server. Required by retrofit for the specific Api-Call.
     * The name of the file cannot be specified for the server and is always "mission.json"
     */
    public interface FileUploadService {
        @Multipart
        @POST("/uploadMission")
        Call<ResponseBody> upload(
                @Part("description") RequestBody description,
                @Part MultipartBody.Part file
        );
    }

    /**
     * This method is used to ensure the existence of specific directories in the internal app storage.
     * The path to save the files for a specific waypoint is determined by the mission_id and the waypoint number.
     * In the case that these directories do not exist at the time when we want to save a file this method creates them.
     * @param mission The mission_id to generate the path.
     * @param waypoint The waypoint number to generate the path.
     */
    private void checkDirectories(String mission, int waypoint){
        String base_path = repository.getContext().getFilesDir()+"/missions/mission_"+mission+"/waypoint_"+waypoint;
        File file = new File(base_path);
        if(!file.exists()){
            file.mkdirs();
        }
    }

    /**
     * This method is used to write a file, that is returned in the response-body following an Api-Call, to the internal app storage.
     * @param mission The mission_id to generate the path.
     * @param waypoint The waypoint number to generate the path.
     * @param filename The filename to generate the path.
     * @param body The response-body containing the file.
     * @return boolean value that tells if the writing operation was successful.
     */
    private boolean writeResponseBodyToDisk(String mission, int waypoint, String filename,ResponseBody body) {
        try {
            checkDirectories(mission, waypoint);
            String path = repository.getContext().getFilesDir()+"/missions/mission_"+mission+"/waypoint_"+waypoint+"/"+filename;
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
