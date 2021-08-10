package com.paltech.dronesncars.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverRoute;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A subclass of {@link MapFragment}, which adds functionality to display and modify pre-computed
 * rover routes.
 */
public class RoverMap extends MapFragment {

    /**
     * List of Polylines, the overlays that are used to display a rover route on the map. List
     * instead of a single polyline, because a rover's route is split into two different colored
     * parts, one where it already was and one where it still has to go.
     */
    private List<Polyline> observed_rover_route;

    /**
     * List of RoverRoutes
     */
    private List<RoverRoute> rover_routes;

    /**
     * the currently observed rover (has been selected by the user in the RoverStatusFragment) which
     * is used to determine which route to display.
     */
    private Rover observed_rover;

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     * sets the buttonEditRoute visible and enabled.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observed_rover_route = new ArrayList<>();

        view_binding.buttonEditRoute.setEnabled(false);
        view_binding.buttonEditRoute.setVisibility(View.GONE);
    }


    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     * Overrides the corresponding method {@link MapFragment#set_livedata_sources()} from its
     * superclass in order to add additional LiveData-Sources:
     * 1. get_rover_routes() -> (re-)compute the rover route overlay for the currently observed
     * route when the route has changed
     * 2. status_observed_rover -> (re-)compute the rover route overlay for the currently observed
     * rover status has changed, e.g. the position or last waypoint has been updated.
     */
    @Override
    protected  void set_livedata_sources() {
        super.set_livedata_sources();

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

    /**
     * DO NOTHING to observe the rover routes in the view_model. this fragment is not concerned with
     * all rover routes (as opposed to its superclass) but only the one of the currently selected
     * rover.
     */
    @Override
    protected void observe_rover_routes() {
    }

    /**
     * remove the currently displayed rover route from the map (not from the database!)
     */
    private void clear_current_observed_route() {
        if (observed_rover_route != null && !observed_rover_route.isEmpty()) {
            view_binding.map.getOverlayManager().removeAll(observed_rover_route);
            observed_rover_route = new ArrayList<>();
            view_binding.map.invalidate();
        }
    }

    /**
     * compute a new overlay for currently observed rover. Split the rover route at the current
     * position of the rover into two different colored polylines: green for places it already
     * visited and red for places it still has to visit. The result will be saved in
     * {@link #observe_rover_routes} and displayed on the map.
     */
    private void compute_new_observed_route_overlay() {
        if (rover_routes != null && !rover_routes.isEmpty() && observed_rover != null) {
            RoverRoute observed_route = null;
            List<RoverRoute> current_rover_routes = rover_routes.stream().filter(rover_route -> rover_route_ids_in_routine.contains(rover_route.rover_route_id)).collect(Collectors.toList());
            for (RoverRoute route : current_rover_routes) {
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
                        List<GeoPoint> driven = new ArrayList<>(route.subList(0, observed_rover.currentWaypoint-1));
                        if (0 == observed_rover.currentWaypoint - 1) {
                            driven.add(route.get(0));
                        }
                        List<GeoPoint> not_driven = new ArrayList<>(route.subList(observed_rover.currentWaypoint, route.size()-1)); // TODO do we need route.size() as ending boundary instead?
                        if (observed_rover.currentWaypoint == route.size()-1) {
                            not_driven.add(route.get(observed_rover.currentWaypoint));
                        }

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
        } else if (observed_rover == null) {
            clear_current_observed_route();
        }
    }

    /**
     * check if it is necessary to compute a new overlay for the given rover
     * @param new_rover the rover to check for
     * @return true if observed_rover is different from new_rover
     */
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