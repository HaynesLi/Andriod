package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentRoverRouteBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoverRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoverRouteFragment extends LandscapeFragment<FragmentRoverRouteBinding, ViewModel> {

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
    FragmentRoverRouteBinding get_view_binding(View view) {
        return FragmentRoverRouteBinding.bind(view);
    }

    @Override
    ViewModel get_view_model() {
        return null;
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

        view_binding = get_view_binding(view);
    }
}