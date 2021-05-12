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

public class StartScreen extends Fragment {

    private FragmentStartScreenBinding view_binding;

    private ActivityResultLauncher<String> getKML = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    Toast.makeText(getContext(),
                            String.format("Got Uri: %s", result.toString()),
                            Toast.LENGTH_SHORT).show();
                    changeToDroneScreen();
                }
            });

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

        view_binding.manualMapButton.setOnClickListener(v -> changeToDroneScreen());
    }

    private void changeToDroneScreen() {
        NavDirections actionStartScreenToDroneScreen = StartScreenDirections.actionStartScreenToDroneScreen();
        NavHostFragment.findNavController(this).navigate(actionStartScreenToDroneScreen);
    }
}