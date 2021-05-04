package com.paltech.dronesncars;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paltech.dronesncars.databinding.FragmentRoverStatusBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverStatusFragment extends Fragment {

    FragmentRoverStatusBinding view_binding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_rover_status, container, false);
    }

    private void init_rover_status_recycler_view() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        view_binding.roverStatusRecyclerView.setLayoutManager(mLayoutManager);
        view_binding.roverStatusRecyclerView.scrollToPosition(0);

        List<Rover> dataset = getMockRoverDataset();
        view_binding.roverStatusRecyclerView.setAdapter(new RoverStatusRecyclerAdapter(dataset));
    }

    private List<Rover> getMockRoverDataset(){
        List<Rover> rover_mock_dataset = new ArrayList<>();

        Rover rover = new Rover();
        rover.rid = 0;
        rover.roverName = "I am only a mock :(";

        rover_mock_dataset.add(rover);

        return rover_mock_dataset;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentRoverStatusBinding.bind(view);
        init_rover_status_recycler_view();

        setListeners();
    }

    private void setListeners(){
        view_binding.buttonAssumeFinished.setOnClickListener(v -> {
            NavDirections action = RoverStatusFragmentDirections.actionRoverStatusFragmentToReportFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
    }
}