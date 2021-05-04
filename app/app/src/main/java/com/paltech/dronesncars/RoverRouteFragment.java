package com.paltech.dronesncars;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paltech.dronesncars.databinding.FragmentRoverRouteBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRouteFragment extends Fragment {

    private FragmentRoverRouteBinding view_binding;

    public RoverRouteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoverRouteFragment.
     */
    public static RoverRouteFragment newInstance(String param1, String param2) {
        RoverRouteFragment fragment = new RoverRouteFragment();
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
        return inflater.inflate(R.layout.fragment_rover_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentRoverRouteBinding.bind(view);
    }
}