package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentRoverStatusBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverStatusFragment extends LandscapeFragment<FragmentRoverStatusBinding, RoverStatusViewModel> implements RoverStatusRecyclerAdapter.OnRoverStatusItemClickedListener, RoverMilestonesRecyclerAdapter.OnRoverMilestonesItemClickedListener {

    FragmentRoverStatusBinding view_binding;
    RoverStatusViewModel view_model;
    MapViewModel map_view_model;
    Timer timer;
    Rover selected_rover;

    private RoverStatusRecyclerAdapter roverStatusAdapter;
    private RoverMilestonesRecyclerAdapter roverMilestonesAdapter;

    public RoverStatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoverStatusFragment.
     */
    public static RoverStatusFragment newInstance(String param1, String param2) {
        RoverStatusFragment fragment = new RoverStatusFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    FragmentRoverStatusBinding get_view_binding(View view) {
        return FragmentRoverStatusBinding.bind(view);
    }

    @Override
    RoverStatusViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(RoverStatusViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_rover_status, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();
        map_view_model = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        init_rover_status_recycler_view();
        init_rover_milestones_recycler_view();
        setLiveDataSources();
        setListeners();
    }

    @Override
    public void onPause() {
        timer.cancel();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = view_model.startRoverUpdates();
    }

    private void init_rover_status_recycler_view() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        view_binding.roverStatusRecyclerView.setLayoutManager(mLayoutManager);
        view_binding.roverStatusRecyclerView.scrollToPosition(0);

        roverStatusAdapter = new RoverStatusRecyclerAdapter(new ArrayList<>(), this);
        view_binding.roverStatusRecyclerView.setAdapter(roverStatusAdapter);
    }

    private void init_rover_milestones_recycler_view() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        view_binding.roverMilestoneRecyclerView.setLayoutManager(mLayoutManager);
        view_binding.roverMilestoneRecyclerView.scrollToPosition(0);

        roverMilestonesAdapter = new RoverMilestonesRecyclerAdapter(new ArrayList<>(),this);
        view_binding.roverMilestoneRecyclerView.setAdapter(roverMilestonesAdapter);
    }


    private void setLiveDataSources() {
        view_model.getUsedRovers().observe(getViewLifecycleOwner(),
                rovers -> roverStatusAdapter.setLocalRoverSet(rovers));
    }

    private void setListeners(){
        view_binding.buttonAssumeFinished.setOnClickListener(v -> {
            NavDirections action = RoverStatusFragmentDirections.actionRoverStatusFragmentToReportFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
        view_binding.buttonMockProgressUpdate.setOnClickListener(v -> view_model.mock_progress_update());
    }

    @Override
    public void onRoverStatusItemClicked(Rover clicked_rover) {
        if (clicked_rover != null) {
            map_view_model.set_status_observed_rover(clicked_rover);
            List<Waypoint> waypoint_list = new ArrayList<>();
            if(clicked_rover.equals(selected_rover)){
                selected_rover = null;
                roverMilestonesAdapter.setLocalWaypointSet(waypoint_list);
            }else {
                this.selected_rover = clicked_rover;
                for (int i = 0; i < clicked_rover.waypoints.size(); i++) {
                    Waypoint waypoint = clicked_rover.waypoints.get(i);
                    if (waypoint.milestone_completed && !waypoint.is_navigation_point) {
                        waypoint_list.add(waypoint);
                    }
                }
                roverMilestonesAdapter.setLocalWaypointSet(waypoint_list);
            }
        }
    }

    @Override
    public void onRoverMilestonesItemClicked(Waypoint clicked_waypoint) {
        if (clicked_waypoint != null) {
            show_milestone_popUp(clicked_waypoint);
        }
    }

    private void show_milestone_popUp(Waypoint clicked_waypoint) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        final View my_dialog_view = inflater.inflate(R.layout.milestone_popup, null);
        final AlertDialog my_dialog = new AlertDialog.Builder(requireContext()).create();
        my_dialog.setView(my_dialog_view);

        String pathPicPrevious = requireContext().getFilesDir()+"/Milestones/Mission_"+clicked_waypoint.mission_id+"/Waypoint_"+clicked_waypoint.waypoint_number+"/previous.jpeg";
        File imgFilePrevious = new  File(pathPicPrevious);
        if(imgFilePrevious.exists()){
            ImageView image = my_dialog_view.findViewById(R.id.picPrevious);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFilePrevious.getAbsolutePath());
            image.setImageBitmap(myBitmap);
        }else{
            Log.d("RoverStatusFragment", "File PicPrevious not found");
        }

        String pathPicAfter = requireContext().getFilesDir()+"/Milestones/Mission_"+clicked_waypoint.mission_id+"/Waypoint_"+clicked_waypoint.waypoint_number+"/after.jpeg";
        File imgFileAfter = new  File(pathPicAfter);
        if(imgFileAfter.exists()){
            ImageView image = my_dialog_view.findViewById(R.id.picAfter);
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFileAfter.getAbsolutePath());
            image.setImageBitmap(myBitmap);
        }else{
            Log.d("RoverStatusFragment", "File PicAfter not found");
        }

        TextView timeText = my_dialog_view.findViewById(R.id.timeText);
        TextView confidenceText = my_dialog_view.findViewById(R.id.confidenceText);
        TextView depthText = my_dialog_view.findViewById(R.id.depthText);

        String pathWaypointData = requireContext().getFilesDir()+"/Milestones/Mission_"+clicked_waypoint.mission_id+"/Waypoint_"+clicked_waypoint.waypoint_number+"/waypoint_data.json";
        try {
            String jsonString = readFile(pathWaypointData, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);
            timeText.setText(jsonObject.getString("time_in_seconds"));
            confidenceText.setText(jsonObject.getString("confidence"));
            depthText.setText(jsonObject.getString("depth_in_cm"));

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        TextView waypointText = my_dialog_view.findViewById(R.id.waypointText);
        waypointText.setText("Waypoint "+clicked_waypoint.waypoint_number);
        TextView missionText = my_dialog_view.findViewById(R.id.missionText);
        missionText.setText("Mission "+clicked_waypoint.mission_id);

        my_dialog.show();
        my_dialog.getWindow().setLayout(832, 388);
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}