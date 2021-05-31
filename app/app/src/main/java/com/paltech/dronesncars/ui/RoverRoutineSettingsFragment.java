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
import com.paltech.dronesncars.databinding.FragmentRoverRoutineSettingsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRoutineSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRoutineSettingsFragment extends LandscapeFragment {

    private FragmentRoverRoutineSettingsBinding view_binding;
    private RoverRoutineSettingsViewModel view_model;

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
        view_model = new ViewModelProvider(requireActivity()).get(RoverRoutineSettingsViewModel.class);

        setLiveDataSources();
        setListeners();
    }

    private void setLiveDataSources() {
        view_model.num_of_rovers.observe(getViewLifecycleOwner(), num_of_rovers -> {
            if (num_of_rovers != 0) {
                view_binding.numOfRoversInput.setText(num_of_rovers.toString());
                view_model.add_rovers(num_of_rovers);
            }
        });
    }

    private void setListeners() {
        view_binding.computeRoutineButton.setOnClickListener(v -> {
            view_model.set_num_of_rovers(Integer.parseInt(view_binding.numOfRoversInput.getText().toString()));
        });

        view_binding.acceptRoverRoutineButton.setOnClickListener(v -> {
            NavDirections action = RoverRouteFragmentDirections.actionRoverRouteFragmentToRoverStatusFragment();
            RoverRouteFragment parentFragment = (RoverRouteFragment) getParentFragment();
            if (parentFragment != null) {
                NavHostFragment.findNavController(parentFragment).navigate(action);
            }
        });
    }
}