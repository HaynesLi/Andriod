package com.paltech.dronesncars.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.ArraySet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.paltech.dronesncars.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration app_bar_configuration;
    private Toolbar toolbar;
    private NavController nav_controller;
    private DrawerLayout drawer_layout;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer_layout = findViewById(R.id.drawer_layout);
        NavigationView nav_view = findViewById(R.id.nav_view);
        nav_controller = Navigation.findNavController(this, R.id.nav_host_fragment);

        Set<Integer> primary_destinations = new ArraySet<>();
        primary_destinations.add(R.id.startScreen);
        primary_destinations.add(R.id.droneScreen);
        primary_destinations.add(R.id.scanResultsFragment);
        primary_destinations.add(R.id.roverRouteFragment);
        primary_destinations.add(R.id.roverStatusFragment);
        primary_destinations.add(R.id.reportFragment);

        AppBarConfiguration.Builder app_bar_config_builder = new AppBarConfiguration.Builder(primary_destinations);
        app_bar_config_builder.setOpenableLayout(drawer_layout);

        app_bar_configuration = app_bar_config_builder.build();

        //NavigationUI.setupActionBarWithNavController(this, nav_controller, app_bar_configuration);
        NavigationUI.setupWithNavController(toolbar, nav_controller, app_bar_configuration);
        NavigationUI.setupWithNavController(nav_view, nav_controller);

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