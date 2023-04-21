package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentDroneSettingsBinding;

/**
 * The Fragment which holds the UI for configuring different parameters for the Drone, such as the
 * flight altitude. It is a subclass of {@link LandscapeFragment}.
 */
public class DroneSettingsFragment extends LandscapeFragment<FragmentDroneSettingsBinding, DroneSettingsViewModel> {

    /**
     * A required empty public constructor
     */
    public DroneSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DroneSettingsFragment.
     */
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

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drone_settings, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     * 1. gets ViewBinding
     * 2. gets ViewModel
     * 3. triggers configuration of LiveData-Sources
     * 4. triggers configuration of Listeners
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();

        set_livedata_sources();
        set_listeners();
    }

    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     * LiveData-Sources:
     * 1. flight_altitude -> if the altitude set in the database is different from the currently set
     * altitude in the UI, change the UI's value
     */
    private void set_livedata_sources() {
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

    /**
     * Configure the ClickListeners for two buttons:
     * 1. buttonStartFlight -> navigate to the {@link ScanResultsFragment}
     * 2. buttonComputeRoute -> collect the currently set flight altitude and start computation of
     * the currently best flight route
     */
    private void set_listeners() {
        view_binding.buttonStartFlight.setOnClickListener(v -> {
            NavDirections action = DroneScreenDirections.actionDroneScreenToScanResultsFragment();
            DroneScreen parentFragment = (DroneScreen) getParentFragment();
            if (parentFragment != null) {
                NavHostFragment.findNavController(parentFragment).navigate(action);
            }
        });
        view_binding.buttonComputeRoute.setOnClickListener(v -> {
            String altitude_string = view_binding.editTextFlightAltitude.getText().toString();
            if (!"".equals(altitude_string)) {
                view_model.setFlightAltitude(Integer.parseInt(
                        altitude_string));
                view_model.computeRoute();
            }
        });
    }
}