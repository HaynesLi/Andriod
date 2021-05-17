package com.paltech.dronesncars.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentStartScreenBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StartScreen extends Fragment {

    private FragmentStartScreenBinding view_binding;

    private ActivityResultLauncher<String> getKML = registerForActivityResult(new ActivityResultContracts.GetContent(),
            result -> changeToDroneScreen(result));

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_screen, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentStartScreenBinding.bind(view);

        setListeners();
    }

    private void setListeners() {
        view_binding.importKMLButton.setOnClickListener(v -> getKML.launch("application/vnd.google-earth.kml+xml"));

        view_binding.manualMapButton.setOnClickListener(v -> changeToDroneScreen(null));
    }

    private void changeToDroneScreen(Uri kml_file_uri) {
        String uri_string = null;
        if (kml_file_uri != null) {
            uri_string = kml_file_uri.toString();
        }
        NavDirections action = StartScreenDirections.actionStartScreenToDroneScreen(uri_string);
        NavHostFragment.findNavController(this).navigate(action);
    }
}