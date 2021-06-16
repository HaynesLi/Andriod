package com.paltech.dronesncars.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentStartScreenBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StartScreen extends LandscapeFragment<FragmentStartScreenBinding, ViewModel> {

    private FragmentStartScreenBinding view_binding;

    private final ActivityResultLauncher<String> getKML = registerForActivityResult(new ActivityResultContracts.GetContent(),
            this::changeToDroneScreen);

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

        view_binding = get_view_binding(view);

        setListeners();
    }

    @Override
    FragmentStartScreenBinding get_view_binding(View view) {
        return FragmentStartScreenBinding.bind(view);
    }

    @Override
    ViewModel get_view_model() {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private class myCallback extends WifiManager.LocalOnlyHotspotCallback {
        @Override
        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
            WifiConfiguration soft_AP_config = reservation.getWifiConfiguration();
            Toast.makeText(requireContext(), soft_AP_config.SSID + ": " + soft_AP_config.preSharedKey , Toast.LENGTH_LONG).show();
            Log.d("help_hotspot", soft_AP_config.preSharedKey);
        }

        @Override
        public void onStopped() {
            Toast.makeText(requireContext(), "stopped", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed(int reason) {
            Toast.makeText(requireContext(), "failed: " + reason, Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void start_hotspot() {
        WifiManager wifiManager = (WifiManager)
                requireContext().getSystemService(Context.WIFI_SERVICE);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        wifiManager.startLocalOnlyHotspot(new myCallback(), null);


    }

    private void setListeners() {
        view_binding.importKMLButton.setOnClickListener(v -> getKML.launch("application/vnd.google-earth.kml+xml"));

        view_binding.manualMapButton.setOnClickListener(v -> {
            //start_hotspot();
            changeToDroneScreen(null);
        });
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