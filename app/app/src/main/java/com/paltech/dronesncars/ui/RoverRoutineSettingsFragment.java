package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentRoverRoutineSettingsBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * A Fragment used to configure the different rovers for weed picking. Allows the user to register
 * new rovers, delete them, select connected ones for work and compute routes for them. Is a
 * subclass of {@link LandscapeFragment}.
 */
public class RoverRoutineSettingsFragment extends LandscapeFragment<FragmentRoverRoutineSettingsBinding, RoverRoutineSettingsViewModel> {

    private FragmentRoverRoutineSettingsBinding view_binding;
    private RoverRoutineSettingsViewModel view_model;

    /**
     * The RecyclerAdapter used to configure the RoverConfiguration-Recycler-View
     */
    RoverConfigurationRecyclerAdapter roverConfigurationRecyclerAdapter;

    /**
     * A boolean used to determine  whether the RoverConfiguration-Recycler-View is supposed to show
     * all rovers or only the ones currently connected
     */
    private boolean displayAllRovers;

    /**
     * The timer used to schedule connection updates with the rovers.
     */
    private Timer timer;

    /**
     * A list of all rovers used to filter the ones out that are not supposed to be shown in the
     * RoverConfiguration-Recycler-View
     */
    private List<Rover> allRovers;

    public RoverRoutineSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoverRoutineSettingsFragment.
     */
    public static RoverRoutineSettingsFragment newInstance() {
        RoverRoutineSettingsFragment fragment = new RoverRoutineSettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    FragmentRoverRoutineSettingsBinding get_view_binding(View view) {
        return FragmentRoverRoutineSettingsBinding.bind(view);
    }

    @Override
    RoverRoutineSettingsViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(RoverRoutineSettingsViewModel.class);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rover_routine_settings, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     * 1. gets ViewBinding
     * 2. gets ViewModel
     * 3. triggers configuration of the rover configuration recycler view
     * 4. triggers configuration of LiveData-Sources
     * 5. triggers configuration of Listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();

        init_rover_configuration_recycler_view();
        setLiveDataSources();
        setListeners();
        displayAllRovers = false;
    }

    /**
     * A basic lifecycle method of an android fragment, used to additionally stop the {@link #timer}
     */
    @Override
    public void onPause() {
        timer.cancel();
        super.onPause();
    }

    /**
     * A basic lifecycle method of an android fragment, used to additionally resume the
     * {@link #timer}
     */
    @Override
    public void onResume() {
        super.onResume();
        timer = view_model.startRoverUpdates();
    }

    /**
     * configure the RoverConfiguration-Recycler-View (for the first time)
     */
    private void init_rover_configuration_recycler_view() {
        RecyclerView.LayoutManager my_layout_manager = new LinearLayoutManager(requireActivity());
        view_binding.roverConfigurationList.setLayoutManager(my_layout_manager);
        view_binding.roverConfigurationList.scrollToPosition(0);
        roverConfigurationRecyclerAdapter = new RoverConfigurationRecyclerAdapter(view_model);
        view_binding.roverConfigurationList.setAdapter(roverConfigurationRecyclerAdapter);
    }

    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     * 1. get_all_rovers_livedata() -> update the {@link #allRovers} variable and filter the
     * not connected ones out
     * 2. get_num_of_used_rovers() -> update the corresponding UI TextView
     * 3. get_num_of_connected_rovers() -> update the corresponding UI TextView
     * 4. get_num_of_rovers() -> update the corresponding UI TextView
     */
    private void setLiveDataSources() {
        view_model.get_all_rovers_livedata().observe(getViewLifecycleOwner(), rovers -> {
            allRovers = rovers;
            get_active_rovers(displayAllRovers);
        });

        view_model.get_num_of_used_rovers().observe(getViewLifecycleOwner(), num_of_used_rovers -> {
            view_binding.numOfRoversInput.setText("Number of selected Rovers: "+num_of_used_rovers.toString());
        });

        view_model.get_num_of_connected_rovers().observe(getViewLifecycleOwner(), num_connected_rovers ->{
            String tmp = view_binding.numOfConnectedRoversInput.getText().toString();
            view_binding.numOfConnectedRoversInput.setText("Number of connected Rovers: "+num_connected_rovers.toString()+"/"+tmp.split("/")[1]);
        });

        view_model.get_num_of_rovers().observe(getViewLifecycleOwner(), num_rovers ->{
            String tmp = view_binding.numOfConnectedRoversInput.getText().toString();
            view_binding.numOfConnectedRoversInput.setText(tmp.split("/")[0]+"/"+num_rovers.toString());
        });
    }

    /**
     * configure the listeners
     * 1. computeRoutineButton -> trigger asynchronous rover-route computation. results will be
     * saved in the database
     * 2. acceptRoverRoutineButton -> associate the currently used rovers to routes and trigger a
     * view-change to the next fragment {@link RoverStatusFragment}
     * 3. buttonEditRoversConfigure -> display all rovers or only the connected ones, depending on
     * the current state of the RoverConfiguration-Recycler-View
     * 4. buttonAddRover -> display a dialog which allows the user to specify a rover name and ip
     * address for a new rover to add
     */
    private void setListeners() {
        view_binding.computeRoutineButton.setOnClickListener(v -> view_model.start_rover_routes_computation());

        view_binding.acceptRoverRoutineButton.setOnClickListener(v -> {
            view_model.associate_rovers_to_routes(this::show_toast);
            NavDirections action = RoverRouteFragmentDirections.actionRoverRouteFragmentToRoverStatusFragment();
            RoverRouteFragment parentFragment = (RoverRouteFragment) getParentFragment();
            if (parentFragment != null) {
                NavHostFragment.findNavController(parentFragment).navigate(action);
            }
        });

        view_binding.buttonEditRoversConfigure.setOnClickListener(v -> {
            if ("Edit".contentEquals(view_binding.buttonEditRoversConfigure.getText())) {
                // TODO extract to string resource
                view_binding.buttonEditRoversConfigure.setText("Cancel");
                view_binding.buttonAddRover.setEnabled(false);
                displayAllRovers = true;
            } else if ("Cancel".contentEquals(view_binding.buttonEditRoversConfigure.getText())) {
                view_binding.buttonEditRoversConfigure.setText(R.string.button_edit_rovers_configure_label);
                view_binding.buttonAddRover.setEnabled(true);
                displayAllRovers = false;
            }
            roverConfigurationRecyclerAdapter.setEditable(displayAllRovers);
            get_active_rovers(displayAllRovers);
        });

        view_binding.buttonAddRover.setOnClickListener(v -> show_name_and_ip_alert_dialog());
    }

    /**
     * set the displayed rovers in the RoverConfiguration-Recycler-View to either all rovers or
     * only the connected ones depending on {@link #displayAllRovers}.
     * @param displayAllRovers all rovers to consider
     */
    public void get_active_rovers(boolean displayAllRovers){
        if(displayAllRovers) {
            roverConfigurationRecyclerAdapter.set_local_rover_set(allRovers);
        }else {
            List<Rover> activeRovers = new ArrayList<>();
            for (int i = 0; i < allRovers.size(); i++) {
                if (allRovers.get(i).status == RoverStatus.CONNECTED) {
                    activeRovers.add(allRovers.get(i));
                }
            }
            roverConfigurationRecyclerAdapter.set_local_rover_set(activeRovers);
        }
    }

    /**
     * display the dialog which allows the user to specify the name and ip address of a new rover
     * and add it to the database
     */
    private void show_name_and_ip_alert_dialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        final View my_dialog_view = inflater.inflate(R.layout.rover_configuration_alert_dialog, null);
        final AlertDialog my_dialog = new AlertDialog.Builder(requireContext()).create();
        my_dialog.setView(my_dialog_view);

        EditText rover_name = my_dialog_view.findViewById(R.id.alert_dialog_rover_name_content);
        EditText rover_ip = my_dialog_view.findViewById(R.id.alert_dialog_rover_inetaddress_content);
        Button add_rover_button = my_dialog_view.findViewById(R.id.button_finish_configure_dialog);
        Button cancel_button = my_dialog_view.findViewById(R.id.button_cancel_configure_dialog);

        add_rover_button.setOnClickListener(v -> {
            String name = rover_name.getText().toString();
            String ip = rover_ip.getText().toString();
            if (!"".equals(name) && !"".equals(ip)) {
                try {
                    view_model.add_Rover(name, InetAddress.getByName(ip), this::show_toast);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.d("InetAddress Error", "show_name_and_ip_alert_dialog: not a legit InetAddress");
                }
            }
            my_dialog.dismiss();
        });

        cancel_button.setOnClickListener(v -> my_dialog.dismiss());

        my_dialog.show();
    }
}