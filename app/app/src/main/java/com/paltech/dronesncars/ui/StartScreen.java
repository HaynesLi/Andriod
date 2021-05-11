package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentStartScreenBinding;

public class StartScreen extends Fragment {

    private FragmentStartScreenBinding view_binding;

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
        view_binding.importKMLButton.setOnClickListener(v -> changeToDroneScreen());

        view_binding.manualMapButton.setOnClickListener(v -> changeToDroneScreen());
    }

    private void changeToDroneScreen() {
        NavDirections actionStartScreenToDroneScreen = StartScreenDirections.actionStartScreenToDroneScreen();
        NavHostFragment.findNavController(this).navigate(actionStartScreenToDroneScreen);
    }
}