package com.paltech.dronesncars.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.paltech.dronesncars.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {


    private static final List<String> PERMISSIONS = Arrays.asList(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CHANGE_WIFI_STATE"
    );

    private static final int PERMISSION_REQUEST_CODE = 42069;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    /*
    TODO: request permissions
    TODO: this isn't exactly best practice, as soon as we know which permissions we need to
     keep we should "move" the permission request to the beginning of whatever behaviour needs
     the corresponding permission
     */
    private void requestPermissions() {
        List<String> permissions_to_request = new ArrayList<>();

        for (String permission : PERMISSIONS) {
            int granted = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                permissions_to_request.add(permission);
            }
        }

        if (!permissions_to_request.isEmpty()) {
            String[] temporary_string_copy = Arrays.copyOf(permissions_to_request.toArray(),
                    permissions_to_request.size(), String[].class);
            requestPermissions(temporary_string_copy, PERMISSION_REQUEST_CODE);
        }
    }

}