package com.paltech.dronesncars.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;
import com.paltech.dronesncars.model.RoverRoute;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The Fragment that holds an OSMDroid-Map and a set of buttons, some of which are only required by
 * subclass of this fragment. Is a subclass of {@link LandscapeFragment}.
 */
@AndroidEntryPoint
public class MapFragment extends LandscapeFragment<FragmentMapBinding, MapViewModel> {

    /**
     * Defines the different edit states a MapFragment can be in.
     * 1. NONE -> currently neither Polygon nor flight-/rover-route are being edited
     * 2. EDIT_POLYGON -> currently the Polygon is being edited
     * 3. EDIT_ROUTE -> currently the flight-/rover-route is being edited
     */
    protected enum VIEW_STATE {NONE, EDIT_POLYGON, EDIT_ROUTE}

    /**
     * Represents the edit state this fragment is currently in
     */
    protected VIEW_STATE current_state;

    /**
     * A Boolean used to determine whether the current route has to be updated in the database after
     * exiting the EDIT_ROUTE state.
     */
    protected boolean changed_during_edit;

    /**
     * The {@link Marker} currently selected by the User in the {@link org.osmdroid.views.MapView}
     */
    protected Marker selected_marker = null;

    /**
     * A List of Lists of Markers which specify the nodes defining a list of routes
     */
    private List<List<Marker>> edit_rover_route_markers;

    /**
     * A List of Lists of Booleans which stores for each GeoPoint->Marker in the Rover-Routes whether
     * it is a navigation point or not. It is necessary to store this info in order to correctly
     * "label" the GeoPoints in the routes after editing them.
     */
    private List<List<Boolean>> edit_rover_route_is_navigation_point;
    /**
     * A List of the most recently computed rover routes.
     */
    protected List<String> rover_route_ids_in_routine;

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view_binding = FragmentMapBinding.inflate(inflater, container, false);
        return view_binding.getRoot();
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     * 1. gets ViewBinding
     * 2. gets ViewModel
     * 3. sets the initial state ot NONE
     * 4. triggers class specific configuration
     * 5. triggers configuration of the view's buttons
     * 6. triggers configuration of the view's Map
     * 7. triggers configuration of the LiveData-Sources
     * 8. triggers configuration of the listeners
     * 9. makes sure the polygon currently saved in the database is displayed
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);


        view_model = get_view_model();
        current_state = VIEW_STATE.NONE;
        changed_during_edit = false;

        view_binding.buttonEditRoute.setText("Edit Route");

        different_inits();
        configure_buttons();
        configureMap();
        set_livedata_sources();
        set_listeners();
        refresh_polygon();
    }

    protected void different_inits() {
        edit_rover_route_markers = null;
        edit_rover_route_is_navigation_point = null;
    }

    protected void refresh_polygon() {
        view_model.getPolygon();
    }

    /**
     * configures the {@link org.osmdroid.views.MapView} inside the fragment
     */
    protected void configureMap() {
        Context ctx = requireContext();
        // ATTENTION: this configuration is NOT part of android, but of osmdroid!
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        view_binding.map.setTileSource(TileSourceFactory.MAPNIK);
        view_binding.map.setMultiTouchControls(true);

        IMapController mapController = view_binding.map.getController();
        mapController.setZoom(9.5);

        view_binding.map.invalidate();
    }

    protected void set_edit_buttons_enabled(boolean enabled) {
        view_binding.buttonEditRoute.setEnabled(enabled);
    }

    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     */
    protected void set_livedata_sources() {
        // draw the "new" polygon if it changed in the database
        view_model.polygon.observe(getViewLifecycleOwner(), polygon -> {
            if (polygon != null) {
                set_polygon(polygon);
                set_edit_buttons_enabled(true);
            } else {
                find_and_delete_overlay("both", true);
            }
        });

        view_model.get_rover_routine_livedata().observe(getViewLifecycleOwner(), rover_routine -> {
            if (rover_routine == null) {
                rover_route_ids_in_routine = new ArrayList<>();
            } else {
                rover_route_ids_in_routine = rover_routine.rover_route_ids;
            }
        });

        observe_rover_routes();
    }

    /**
     * configures the LiveData-Source for observing the rover-routes in a separate method to allow
     * subclasses to override specifically this task
     */
    protected void observe_rover_routes() {
        view_model.get_rover_routes().observe(getViewLifecycleOwner(), routes -> {
            if (routes == null || routes.isEmpty()) {
                return;
            }

            // TODO this probably is not very efficient: if we compute A LOT of routes over time and
            //  never delete any from the database we have to filter all of them here just to
            //  draw the few we need to draw...
            List<RoverRoute> in_routine = routes.stream().filter(rover_route -> rover_route_ids_in_routine.contains(rover_route.rover_route_id)).collect(Collectors.toList());
            List<List<GeoPoint>> drawable_routes = new ArrayList<>();
            List<List<Boolean>> is_navigation_point_list = new ArrayList<>();
            for (RoverRoute rover_route: in_routine) {
                List<GeoPoint> route = rover_route.route;
                clear_route_edit_markers();
                edit_rover_route_markers = null;
                edit_rover_route_is_navigation_point = null;

                if (route != null && route.size() > 1) {
                    find_and_delete_overlay("polyline", true);

                    drawable_routes.add(route);
                    is_navigation_point_list.add(rover_route.is_navigation_point);
                }
            }

            List<List<Marker>> tmp = draw_routes(drawable_routes);
            if (tmp != null && !tmp.isEmpty()) {
                edit_rover_route_markers = tmp;
                edit_rover_route_is_navigation_point = is_navigation_point_list;
            }
        });

    }


    protected FragmentMapBinding get_view_binding(View view) {
        return FragmentMapBinding.bind(view);
    }

    protected MapViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(MapViewModel.class);
    }

    /**
     * draws the given polygon in the map
     * @param polygon the polygon overlay to draw
     */
    protected void set_polygon(Polygon polygon) {

        polygon.getFillPaint().setColor(Color.parseColor("#1EFFE70E"));
        if (check_for_proper_polygon(polygon)) {
            if (polygon.getActualPoints().size() > 0) {
                polygon_to_markers(polygon);

                view_binding.map.getOverlayManager().removeAll(view_binding.map.getOverlays());
                view_binding.map.getOverlayManager().add(polygon);
                view_binding.map.invalidate();

                IMapController mapController = view_binding.map.getController();
                mapController.setCenter(polygon.getActualPoints().get(0));
                mapController.setZoom(16.0);
            }
        }
    }

    /**
     * a check for "proper polygon" required because the LiveData sometimes deliver polygons with
     * a crash-causing-configuration
     * @param polygon the polygon to check
     * @return true if the polygon is usable, false else
     */
    protected boolean check_for_proper_polygon(Polygon polygon) {
        if (polygon != null) {
            try {
                polygon.getActualPoints();
                polygon.getHoles();
                return true;
            } catch (NullPointerException null_pointer_exception) {
                return false;
            }
        }
        return false;
    }

    protected void polygon_to_markers(Polygon polygon) {}

    /**
     * delete a certain overlay if you do not own a reference object of the overlay
     * @param type "polyline", "polygon" or "both". (both deletes obviously both all polygons and
     *             all polylines)
     * @param multiple a boolean specifying whether to stop after deleting the first overlay of the
     *                 type or not
     */
    protected void find_and_delete_overlay(String type, boolean multiple) {
        List<Overlay> overlays = view_binding.map.getOverlayManager().overlays();
        label:
        for (Overlay overlay : overlays) {
            switch (type) {
                case "polyline":
                    if (overlay.getClass() == Polyline.class) {
                        view_binding.map.getOverlayManager().remove(overlay);
                        if (!multiple) break label;
                    }
                    break;
                case "polygon":
                    if (overlay.getClass() == Polygon.class) {
                        view_binding.map.getOverlayManager().remove(overlay);
                        if (!multiple) break label;
                    }
                    break;
                case "both":
                    if (overlay.getClass() == Polygon.class || overlay.getClass() == Polyline.class) {
                        view_binding.map.getOverlayManager().remove(overlay);
                    }
                    break;
            }
        }
    }

    /**
     * Build a Marker for the {@link org.osmdroid.views.MapView} following a specific frequently
     * used pattern
     * @param geoPoint the GPS GeoPoint where to place the marker
     * @param changed_during_edit_on_drag whether to set the {@link #changed_during_edit}
     *                                    "onDragEnd" or not
     * @param draggable_default if draggable default is false the marker has to be clicked once to
     *                          make it draggable
     * @return the marker
     */
    protected Marker build_edit_marker(GeoPoint geoPoint, boolean changed_during_edit_on_drag, boolean draggable_default) {
        Marker new_marker = new Marker(view_binding.map, requireContext());
        new_marker.setPosition(geoPoint);
        new_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (!draggable_default) {
            new_marker.setOnMarkerClickListener((marker, mapView) -> {
                if (!marker.equals(selected_marker)) {
                    set_marker_selected(marker);
                    view_binding.map.getController().setCenter(marker.getPosition());
                } else {
                    set_marker_unselected(true);
                }
                return true;
            });
        } else {
            new_marker.setDraggable(true);
        }
        if (changed_during_edit_on_drag){
            new_marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    changed_during_edit = true;
                }

                @Override
                public void onMarkerDragStart(Marker marker) {
                }
            });
        }

        return new_marker;
    }

    /**
     * set a marker selected and change its icon to red
     * @param new_selected the marker to select
     */
    protected void set_marker_selected(Marker new_selected) {
        selected_marker = new_selected;
        new_selected.setDraggable(true);
        Drawable selected_marker_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_selected, null);
        new_selected.setIcon(selected_marker_icon);
        new_selected.setAnchor(0.35f, 0.68f);
        view_binding.map.invalidate();
    }

    /**
     * set a marker unselected and change its icon back to the default
     * @param set_null the marker to set unselected
     */
    protected void set_marker_unselected(boolean set_null) {
        Drawable default_marker_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_default, null);
        if (selected_marker != null) {
            selected_marker.setIcon(default_marker_icon);
            selected_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            selected_marker.setDraggable(false);
            view_binding.map.invalidate();
            if (set_null) {
                selected_marker = null;
            }
        }
    }

    /**
     * add the {@link #edit_rover_route_markers} as overlay to the map
     * @return true if the markers have been added, false if not due to missing
     * edit_rover_route_markers list
     */
    protected boolean add_route_edit_markers() {
        if(edit_rover_route_markers != null) {
            for (List<Marker> markers_current_route: edit_rover_route_markers) {
                view_binding.map.getOverlayManager().addAll(markers_current_route);
            }
            return true;
        } else  {
            return false;
        }
    }

    /**
     * remove the {@link #edit_rover_route_markers} from the map
     */
    protected void clear_route_edit_markers() {
        if (edit_rover_route_markers != null && !edit_rover_route_markers.isEmpty()) {
            for (List<Marker> marker_route: edit_rover_route_markers) {
                view_binding.map.getOverlayManager().removeAll(marker_route);
            }
        }
    }

    /**
     * configure the different buttons when entering the EDIT_ROUTE state
     */
    protected void edit_route_button_activate() {
        if (!add_route_edit_markers()) return;

        view_binding.buttonEditRoute.setText("Stop Edit");
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
        view_binding.buttonAddMarker.setVisibility(View.VISIBLE);
        view_binding.buttonDeleteMarker.setVisibility(View.VISIBLE);
        view_binding.buttonPolygonEdit.setEnabled(false);
        current_state = VIEW_STATE.EDIT_ROUTE;
    }

    /**
     * save changed rover routes to the database
     */
    protected void save_route_or_routes() {
        if (changed_during_edit) {
            changed_during_edit = false;
            List<List<GeoPoint>> new_routes = new ArrayList<>();
            for (List<Marker> route_markers: edit_rover_route_markers) {
                new_routes.add(
                        route_markers.stream().map(Marker::getPosition)
                                .collect(Collectors.toList())
                );
            }

            view_model.set_rover_routes(new_routes, edit_rover_route_is_navigation_point);
        }
    }

    /**
     * configure the different buttons when exiting the EDIT_ROUTE state
     */
    protected void edit_route_button_deactivate() {
        clear_route_edit_markers();
        view_binding.buttonEditRoute.setText("Edit Route");
        view_binding.buttonAddMarker.setVisibility(View.INVISIBLE);
        view_binding.buttonDeleteMarker.setVisibility(View.INVISIBLE);

        save_route_or_routes();

        current_state = VIEW_STATE.NONE;
        set_marker_unselected(true);
    }

    /**
     * set listeners for:
     * 1. buttonEditRoute -> trigger the EDIT_ROUTE state
     * 2. buttonDeleteMarker -> delete selected marker
     * 3. buttonAddMarker -> add a marker between the selected marker and the next one
     * 4. buttonExportPolygonToKml -> export the current polygon into a kml file saved in the
     * tablet's storage
     */
    protected void set_listeners() {
        view_binding.buttonEditRoute.setOnClickListener(v -> {
            if (current_state == VIEW_STATE.NONE) {
                edit_route_button_activate();
            } else if (current_state == VIEW_STATE.EDIT_ROUTE){
                edit_route_button_deactivate();

            }
            view_binding.map.invalidate();
        });

        view_binding.buttonDeleteMarker.setOnClickListener(v -> {
            if (selected_marker != null) {
                if (current_state == VIEW_STATE.EDIT_ROUTE) {
                    delete_marker_route_edit();
                } else if (current_state == VIEW_STATE.EDIT_POLYGON) {
                    delete_marker_polygon_edit();
                }
            }
        });

        view_binding.buttonAddMarker.setOnClickListener(v -> {

                if (current_state == VIEW_STATE.EDIT_POLYGON) {
                    add_marker_polygon_edit();
                } else if (current_state == VIEW_STATE.EDIT_ROUTE) {
                    add_marker_route_edit();
                }
        });

        view_binding.buttonExportPolygonToKml.setOnClickListener(v -> view_model.save_polygon_to_kml());
    }


    /**
     * delete the selected marker from its route
     */
    protected void delete_marker_route_edit() {
        int index_of_corresponding_route = 0;
        int index_in_corresponding_route = 0;
        for (List<Marker> marker_route: edit_rover_route_markers) {
            if (marker_route.contains(selected_marker)) {
                index_in_corresponding_route = marker_route.indexOf(selected_marker);
                marker_route.remove(selected_marker);
                view_binding.map.getOverlayManager().remove(selected_marker);
                set_marker_unselected(true);
                changed_during_edit = true;
                view_binding.map.invalidate();
                break;
            }
            index_of_corresponding_route++;
        }

        edit_rover_route_is_navigation_point.get(index_of_corresponding_route).remove(index_in_corresponding_route);
    }

    protected void delete_marker_polygon_edit() {}

    /**
     * add a marker in the map center which is located in the current route right after the
     * currently selected marker
     */
    protected void add_marker_route_edit() {
        Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(),
                true,
                false);

        for (int i = 0; i < edit_rover_route_markers.size(); i++) {
            List<Marker> marker_route = edit_rover_route_markers.get(i);
            List<Boolean> is_navigation_point = edit_rover_route_is_navigation_point.get(i);
            if (marker_route.contains(selected_marker)) {
                edit_rover_route_markers.set(i,
                        insert_marker_at_index(marker_route,
                                marker_route.indexOf(selected_marker),
                                new_marker));
                edit_rover_route_is_navigation_point.set(i,
                        insert_bool_at_index(is_navigation_point,
                                marker_route.indexOf(selected_marker), true));
                set_marker_unselected(false);
                set_marker_selected(new_marker);
                view_binding.map.getOverlayManager().add(new_marker);
                view_binding.map.invalidate();
                changed_during_edit = true;
                break;
            }
        }
    }

    protected void add_marker_polygon_edit() {}

    /**
     * insert a Marker at the given index+1 into the given list
     * @param list the list to insert into
     * @param index the index to insert after
     * @param marker the marker to insert
     * @return the list with the marker inserted
     */
    protected List<Marker> insert_marker_at_index(List<Marker> list, int index, Marker marker) {
        if (index >= 0 && index <= list.size()) {
            List<Marker> before = list.subList(0, index);
            List<Marker> after = list.subList(index, list.size());
            List<Marker> insertion = new ArrayList<>();
            insertion.add(marker);
            return Stream.of(before, insertion, after).flatMap(Collection::stream).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * insert a boolean at the given index+1 into the given list
     * @param list the list to insert into
     * @param index the index to insert after
     * @param bool the boolean to insert
     * @return the list with the boolean inserted
     */
    private List<Boolean> insert_bool_at_index(List<Boolean> list, int index, boolean bool) {
        if (index >= 0 && index <= list.size()) {
            List<Boolean> before = list.subList(0, index);
            List<Boolean> after = list.subList(index, list.size());
            List<Boolean> insertion = new ArrayList<>();
            insertion.add(bool);
            return Stream.of(before, insertion, after).flatMap(Collection::stream).collect(Collectors.toList());
        }
        return null;
    }

    protected void set_center_to_last_location() {
        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint last_location = new GeoPoint(location.getLatitude(), location.getLongitude());
                view_binding.map.getController().setCenter(last_location);
                view_binding.map.getController().setZoom(16.0);
            }
        });
    }

    /**
     * initial configuration if the buttonPolygonEdit and buttonExportPolygonToKml
     */
    protected void configure_buttons() {
        view_binding.buttonPolygonEdit.setEnabled(false);
        view_binding.buttonPolygonEdit.setVisibility(View.INVISIBLE);
        view_binding.buttonExportPolygonToKml.setEnabled(false);
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
    }

    /**
     * generic method to draw a list of routes onto the {@link org.osmdroid.views.MapView}
     * @param routes a list of routes, where each route is a list of geopoints
     * @return the markers which maybe needed for editing the newly drawn routes later
     */
    protected List<List<Marker>> draw_routes(List<List<GeoPoint>> routes) {
        find_and_delete_overlay("polyline", true);
        List<List<Marker>> multiple_routes_markers = new ArrayList<>();
        for (List<GeoPoint> route: routes) {
            List<Marker> route_markers = new ArrayList<>();
            for (GeoPoint point: route) {
                route_markers.add(build_edit_marker(point, true, false));
            }
            multiple_routes_markers.add(route_markers);

            Polyline route_drawable_overlay = new Polyline();
            route_drawable_overlay.setPoints(route);
            route_drawable_overlay.getOutlinePaint().setStrokeWidth(1);

            view_binding.map.getOverlayManager().add(route_drawable_overlay);
            view_binding.map.getController().setCenter(route_drawable_overlay.getActualPoints().get(0));
        }
        view_binding.map.invalidate();
        return  multiple_routes_markers;
    }
}
