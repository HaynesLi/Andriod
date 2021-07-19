package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.model.RoverRoute;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RoverMap extends MapFragment {

    private List<Polyline> observed_rover_route;
    private LiveData<List<RoverRoute>> rover_routes;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding.buttonEditRoute.setEnabled(false);
        view_binding.buttonEditRoute.setVisibility(View.GONE);
    }

    @Override
    protected  void setLiveDataSources() {
        super.setLiveDataSources();

        rover_routes = view_model.get_rover_routes();

        view_model.status_observed_rover.observe(getViewLifecycleOwner(), rover -> {
            if (rover_routes != null && rover != null) {
                List<RoverRoute> current_routes = rover_routes.getValue();
                if (current_routes != null && !current_routes.isEmpty()) {
                    RoverRoute observed_route = null;
                    for (RoverRoute route : current_routes) {
                        if (route.corresponding_rover_id == rover.rover_id) {
                            observed_route = route;
                            break;
                        }
                    }

                    if (observed_route != null) {
                        List<GeoPoint> route = observed_route.route;
                        if (route != null && !route.isEmpty()) {
                            if (rover.currentWaypoint > route.size()) {
                                clear_current_observed_route();
                            } else if (rover.currentWaypoint == 0 || rover.currentWaypoint == route.size()) {
                                clear_current_observed_route();
                                Polyline route_overlay = new Polyline();
                                route_overlay.setPoints(route);
                                if (rover.currentWaypoint == 0) {
                                    route_overlay.getOutlinePaint().setColor(0xff0000); // TODO is this red?
                                } else {
                                    route_overlay.getOutlinePaint().setColor(0x3f6b1c); // TODO is this paltech-green?
                                }

                                this.observed_rover_route.add(route_overlay);
                                view_binding.map.getOverlayManager().addAll(observed_rover_route);
                                view_binding.map.invalidate();
                            } else {
                                clear_current_observed_route();
                                List<GeoPoint> driven = route.subList(0, rover.currentWaypoint);
                                List<GeoPoint> not_driven = route.subList(rover.currentWaypoint+1, route.size()-1); // TODO do we need route.size() as ending boundary instead?
                                GeoPoint position = geopoint_from_string(rover.position);
                                if (position != null) {
                                    driven.add(position); // TODO make rover.position: GeoPoint instead of String!
                                    not_driven.add(0, position);
                                }

                                Polyline driven_overlay = new Polyline();
                                driven_overlay.setPoints(driven);
                                driven_overlay.getOutlinePaint().setColor(0x3f6b1c);

                                Polyline not_driven_overlay = new Polyline();
                                not_driven_overlay.setPoints(not_driven);
                                not_driven_overlay.getOutlinePaint().setColor(0xff0000);

                                this.observed_rover_route.add(driven_overlay);
                                this.observed_rover_route.add(not_driven_overlay);
                                view_binding.map.getOverlayManager().addAll(observed_rover_route);
                                view_binding.map.invalidate();
                            }
                        }
                    }
                }
            }
        });
    }

    private GeoPoint geopoint_from_string(String location) {
        // TODO if we don't change the property to GeoPoint inside Rover itself: add error_handling at this point!
        String[] coordinates = location.split("(,|, )");
        if (coordinates.length >= 2) {
            long latitude = Long.parseLong(coordinates[0]); // TODO is this the right way around?
            long longitude = Long.parseLong(coordinates[1]);
            GeoPoint geopoint = new GeoPoint(latitude, longitude);
            return geopoint;
        }

        return null;
    }

    private void clear_current_observed_route() {
        if (observed_rover_route != null && !observed_rover_route.isEmpty()) {
            view_binding.map.getOverlayManager().removeAll(observed_rover_route);
            observed_rover_route = new ArrayList<>();
            view_binding.map.invalidate();
        }
    }
}