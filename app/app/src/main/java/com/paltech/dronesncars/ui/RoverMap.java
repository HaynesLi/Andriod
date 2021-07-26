package com.paltech.dronesncars.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverRoute;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RoverMap extends MapFragment {

    private List<Polyline> observed_rover_route;
    private List<RoverRoute> rover_routes;
    private Rover observed_rover;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observed_rover_route = new ArrayList<>();

        view_binding.buttonEditRoute.setEnabled(false);
        view_binding.buttonEditRoute.setVisibility(View.GONE);
    }

    @Override
    protected  void setLiveDataSources() {
        super.setLiveDataSources();

        view_model.get_rover_routes().observe(getViewLifecycleOwner(), rover_routes -> {
            this.rover_routes = rover_routes;
            compute_new_observed_route_overlay();
        } );

        view_model.status_observed_rover.observe(getViewLifecycleOwner(), rover -> {
            if(route_overlay_computation_required(rover)) {
                this.observed_rover = rover;
                compute_new_observed_route_overlay();
            } else {
                this.observed_rover = rover;
            }
        });
    }

    @Override
    protected void observe_rover_routes() {
    }

    private void clear_current_observed_route() {
        if (observed_rover_route != null && !observed_rover_route.isEmpty()) {
            view_binding.map.getOverlayManager().removeAll(observed_rover_route);
            observed_rover_route = new ArrayList<>();
            view_binding.map.invalidate();
        }
    }

    private void compute_new_observed_route_overlay() {
        if (rover_routes != null && !rover_routes.isEmpty() && observed_rover != null) {
            RoverRoute observed_route = null;
            for (RoverRoute route : rover_routes) {
                if (route.corresponding_rover_id == observed_rover.rover_id) {
                    observed_route = route;
                    break;
                }
            }

            if (observed_route != null) {
                List<GeoPoint> route = observed_route.route;
                GeoPoint position = observed_rover.position;
                if (route != null && !route.isEmpty()) {
                    if (observed_rover.currentWaypoint > route.size()) {
                        clear_current_observed_route();
                    } else if (observed_rover.currentWaypoint == 0 || observed_rover.currentWaypoint == route.size()) {
                        clear_current_observed_route();
                        Polyline route_overlay = new Polyline();
                        List<GeoPoint> adjusted_route = new ArrayList<>(route);

                        if (observed_rover.currentWaypoint == 0) {
                            route_overlay.getOutlinePaint().setColor(Color.RED); // TODO is this red?
                            if (position != null) {
                                adjusted_route.add(position);
                            }
                        } else {
                            if (position != null) {
                                adjusted_route.add(0, position);
                            }
                            route_overlay.getOutlinePaint().setColor(Color.GREEN); // TODO is this paltech-green?
                        }


                        route_overlay.setPoints(adjusted_route);

                        route_overlay.getOutlinePaint().setStrokeWidth(2);

                        this.observed_rover_route.add(route_overlay);
                        view_binding.map.getOverlayManager().addAll(observed_rover_route);
                        view_binding.map.invalidate();
                    } else {
                        clear_current_observed_route();
                        List<GeoPoint> driven = new ArrayList<>(route.subList(0, observed_rover.currentWaypoint));
                        List<GeoPoint> not_driven = new ArrayList<>(route.subList(observed_rover.currentWaypoint+1, route.size()-1)); // TODO do we need route.size() as ending boundary instead?

                        if (position != null) {
                            driven.add(position);
                            not_driven.add(0, position);
                        } else {
                            driven.add(not_driven.get(0));
                        }

                        Polyline driven_overlay = new Polyline();
                        driven_overlay.setPoints(driven);
                        driven_overlay.getOutlinePaint().setColor(Color.GREEN);
                        driven_overlay.getOutlinePaint().setStrokeWidth(2);

                        Polyline not_driven_overlay = new Polyline();
                        not_driven_overlay.setPoints(not_driven);
                        not_driven_overlay.getOutlinePaint().setColor(Color.RED);
                        not_driven_overlay.getOutlinePaint().setStrokeWidth(2);

                        this.observed_rover_route.add(driven_overlay);

                        this.observed_rover_route.add(not_driven_overlay);
                        view_binding.map.getOverlayManager().addAll(observed_rover_route);
                        view_binding.map.invalidate();
                    }
                }
            }
        }
    }

    private boolean route_overlay_computation_required(Rover new_rover) {
        if (observed_rover == null && new_rover != null) {
            return true;
        }
        if (observed_rover != null && new_rover == null) {
            return true;
        }
        if (observed_rover != null) {
            if (observed_rover.rover_id != new_rover.rover_id) {
                return true;
            }
            if (observed_rover.currentWaypoint != new_rover.currentWaypoint) {
                return true;
            }
            return !observed_rover.position.equals(new_rover.position);
        }
        return false;
    }
}