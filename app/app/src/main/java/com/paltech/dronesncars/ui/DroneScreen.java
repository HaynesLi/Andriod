package com.paltech.dronesncars.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.R;

public class DroneScreen extends LandscapeFragment {

    public Uri getKml_file_uri() {
        return kml_file_uri;
    }

    private Uri kml_file_uri;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment

        kml_file_uri = null;
        String uri_string = DroneScreenArgs.fromBundle(getArguments()).getKmlFileUri();
        if (uri_string != null) {
            kml_file_uri = Uri.parse(uri_string);
        }

        return inflater.inflate(R.layout.fragment_drone_screen, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

}