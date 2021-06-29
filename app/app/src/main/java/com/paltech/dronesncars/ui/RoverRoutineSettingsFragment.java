package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRoutineSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRoutineSettingsFragment extends LandscapeFragment<FragmentRoverRoutineSettingsBinding, RoverRoutineSettingsViewModel> {

    private FragmentRoverRoutineSettingsBinding view_binding;
    private RoverRoutineSettingsViewModel view_model;
    RoverConfigurationRecyclerAdapter roverConfigurationRecyclerAdapter;

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
    }

    private void init_rover_configuration_recycler_view() {
        RecyclerView.LayoutManager my_layout_manager = new LinearLayoutManager(requireActivity());
        view_binding.roverConfigurationList.setLayoutManager(my_layout_manager);
        view_binding.roverConfigurationList.scrollToPosition(0);
        roverConfigurationRecyclerAdapter = new RoverConfigurationRecyclerAdapter(view_model);
        view_binding.roverConfigurationList.setAdapter(roverConfigurationRecyclerAdapter);
    }

    private void setLiveDataSources() {
        view_model.num_of_rovers.observe(getViewLifecycleOwner(), num_of_rovers -> {
            if (num_of_rovers != 0) {
                view_binding.numOfRoversInput.setText(num_of_rovers.toString());
            }
        });
        view_model.get_all_rovers_livedata().observe(getViewLifecycleOwner(), rovers ->
                roverConfigurationRecyclerAdapter.set_local_rover_set(rovers));
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

        view_binding.buttonAddRover.setOnClickListener(v -> {
            try {
                //IpAdresse darf jetzt nicht mehr doppelt vorkommen... führt hier zu Fehler
                view_model.add_Rover("Hubert", InetAddress.getByName("127.0.0.1"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });
    }
}