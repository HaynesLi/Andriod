package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

import com.paltech.dronesncars.R;

/**
 * A Fragment which generally shows the content needed for configuring the drone. Holds the two
 * fragments {@link DroneSettingsFragment} and {@link FlightMapFragment}. It is a subclass of
 * {@link LandscapeFragment}.
 */
public class DroneScreen extends LandscapeFragment<ViewBinding, ViewModel> {

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drone_screen, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     */
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * Unused, as this fragment does not require a ViewBinding
     * @param view the view to get the ViewBinding for
     * @return null
     */
    @Override
    ViewBinding get_view_binding(View view) {
        return null;
    }

    /**
     * Unused, as this fragment does not require a ViewModel
     * @return null
     */
    @Override
    ViewModel get_view_model() {
        return null;
    }
}