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

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentDroneSettingsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DroneSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DroneSettingsFragment extends LandscapeFragment<FragmentDroneSettingsBinding, DroneSettingsViewModel> {

    private FragmentDroneSettingsBinding view_binding;
    private DroneSettingsViewModel view_model;

    public DroneSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DroneSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DroneSettingsFragment newInstance() {
        DroneSettingsFragment fragment = new DroneSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    FragmentDroneSettingsBinding get_view_binding(View view) {
        return FragmentDroneSettingsBinding.bind(view);
    }

    @Override
    DroneSettingsViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(DroneSettingsViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drone_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();

        setLiveDataSources();
        setListeners();
    }

    private void setLiveDataSources() {
        view_model.flight_altitude.observe(getViewLifecycleOwner(), altitude -> {
            if (altitude != 0) {
                String current_altitude = String.valueOf(view_binding.editTextFlightAltitude.getText());
                if (!current_altitude.equals("") && altitude != Integer.parseInt(current_altitude)) {
                    view_binding.editTextFlightAltitude.setText(altitude.toString());
                } else if(current_altitude.equals("")) {
                    view_binding.editTextFlightAltitude.setText(altitude.toString());
                }
            }
        });
    }

    private void setListeners() {
        view_binding.buttonStartFlight.setOnClickListener(v -> {
            NavDirections action = DroneScreenDirections.actionDroneScreenToScanResultsFragment();
            DroneScreen parentFragment = (DroneScreen) getParentFragment();
            if (parentFragment != null) {
                NavHostFragment.findNavController(parentFragment).navigate(action);
            }
        });
        view_binding.buttonComputeRoute.setOnClickListener(v -> {
                view_model.setFlightAltitude(Integer.parseInt(
                        String.valueOf(view_binding.editTextFlightAltitude.getText())));
                view_model.computeRoute();
        });
    }
}