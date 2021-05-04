package com.paltech.dronesncars;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paltech.dronesncars.databinding.FragmentRoverRouteBinding;
import com.paltech.dronesncars.databinding.FragmentRoverRoutineSettingsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRoutineSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRoutineSettingsFragment extends Fragment {

    private FragmentRoverRoutineSettingsBinding view_binding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rover_routine_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentRoverRoutineSettingsBinding.bind(view);

        setListeners();
    }

    private void setListeners() {
        view_binding.acceptRoverRoutineButton.setOnClickListener(v -> {
            NavDirections action = RoverRouteFragmentDirections.actionRoverRouteFragmentToRoverStatusFragment();
            RoverRouteFragment parentFragment = (RoverRouteFragment) getParentFragment();
            NavHostFragment.findNavController(parentFragment).navigate(action);
        });
    }
}