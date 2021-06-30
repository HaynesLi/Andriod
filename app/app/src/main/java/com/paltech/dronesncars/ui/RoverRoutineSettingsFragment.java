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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRoutineSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRoutineSettingsFragment extends LandscapeFragment<FragmentRoverRoutineSettingsBinding, RoverRoutineSettingsViewModel> {

    private FragmentRoverRoutineSettingsBinding view_binding;
    private RoverRoutineSettingsViewModel view_model;
    RoverConfigurationRecyclerAdapter roverConfigurationRecyclerAdapter;

    private Timer timer;

    public RoverRoutineSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoverRoutineSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoverRoutineSettingsFragment newInstance(String param1, String param2) {
        RoverRoutineSettingsFragment fragment = new RoverRoutineSettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    FragmentRoverRoutineSettingsBinding get_view_binding(View view) {
        return FragmentRoverRoutineSettingsBinding.bind(view);
    }

    @Override
    RoverRoutineSettingsViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(RoverRoutineSettingsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rover_routine_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();

        init_rover_configuration_recycler_view();
        setLiveDataSources();
        setListeners();
        timer = view_model.startRoverUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    private void init_rover_configuration_recycler_view() {
        RecyclerView.LayoutManager my_layout_manager = new LinearLayoutManager(requireActivity());
        view_binding.roverConfigurationList.setLayoutManager(my_layout_manager);
        view_binding.roverConfigurationList.scrollToPosition(0);
        roverConfigurationRecyclerAdapter = new RoverConfigurationRecyclerAdapter(view_model);
        view_binding.roverConfigurationList.setAdapter(roverConfigurationRecyclerAdapter);
    }

    private void setLiveDataSources() {
        view_model.get_all_rovers_livedata().observe(getViewLifecycleOwner(), rovers ->
                roverConfigurationRecyclerAdapter.set_local_rover_set(rovers));

        view_model.get_num_of_used_rovers().observe(getViewLifecycleOwner(), num_of_used_rovers -> {
            view_binding.numOfRoversInput.setText(num_of_used_rovers.toString());
        });
    }

    private void setListeners() {
        view_binding.computeRoutineButton.setOnClickListener(v -> view_model.start_rover_routes_computation());

        view_binding.acceptRoverRoutineButton.setOnClickListener(v -> {
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
                set_rover_configuration_items_editable(true);
            } else if ("Cancel".contentEquals(view_binding.buttonEditRoversConfigure.getText())) {
                view_binding.buttonEditRoversConfigure.setText(R.string.button_edit_rovers_configure_label);
                view_binding.buttonAddRover.setEnabled(true);
                set_rover_configuration_items_editable(false);
            }
        });

        view_binding.buttonAddRover.setOnClickListener(v -> show_name_and_ip_alert_dialog());
    }

    private void set_rover_configuration_items_editable(boolean editable) {
        for (int index = 0; index < roverConfigurationRecyclerAdapter.getItemCount(); index++) {
            RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder;
            holder = (RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder)
                    view_binding.roverConfigurationList.findViewHolderForAdapterPosition(index);
            holder.set_editable(editable);
        }
    }

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
                    view_model.add_Rover(name, InetAddress.getByName(ip));
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