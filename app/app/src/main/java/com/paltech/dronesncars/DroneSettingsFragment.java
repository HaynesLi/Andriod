package com.paltech.dronesncars;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paltech.dronesncars.databinding.FragmentDroneSettingsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DroneSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DroneSettingsFragment extends Fragment {

    private FragmentDroneSettingsBinding view_binding;

    public DroneSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DroneSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DroneSettingsFragment newInstance(String param1, String param2) {
        DroneSettingsFragment fragment = new DroneSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        view_binding = FragmentDroneSettingsBinding.bind(view);

        setListeners();
    }

    private void setListeners() {
        view_binding.buttonStartFlight.setOnClickListener(v -> {
            NavDirections action = DroneScreenDirections.actionDroneScreenToScanResultsFragment();
            DroneScreen parentFragment = (DroneScreen) getParentFragment();
            NavHostFragment.findNavController(parentFragment).navigate(action);
        });
    }
}