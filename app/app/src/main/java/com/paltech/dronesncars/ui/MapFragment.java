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

@AndroidEntryPoint
public class MapFragment extends LandscapeFragment<FragmentMapBinding, MapViewModel> {

    protected enum VIEW_STATE {NONE, EDIT_POLYGON, EDIT_ROUTE}
    protected VIEW_STATE current_state;
    protected boolean changed_during_edit;
    protected Marker selected_marker = null;
    private List<List<Marker>> edit_rover_route_markers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view_binding = FragmentMapBinding.inflate(inflater, container, false);
        return view_binding.getRoot();
    }

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
        setLiveDataSources();
        set_click_listeners();
        refresh_polygon();
    }

    protected void different_inits() {
        edit_rover_route_markers = null;
    }

    protected void refresh_polygon() {
        view_model.getPolygon();
    }

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

    protected void setLiveDataSources() {

        // watch dictionary of String (FID) and osmdroid.(...).Polygon in order to, if necessary,
        // display an AlertDialog to make the user chose one of the available polygons


        // draw the "new" polygon if it changed in the database
        view_model.polygon.observe(getViewLifecycleOwner(), polygon -> {
            if (polygon != null) {
                setPolygon(polygon);
                set_edit_buttons_enabled(true);
            } else {
                find_and_delete_overlay("both", true);
            }
        });

        observe_rover_routes();
    }

    protected void observe_rover_routes() {
        view_model.get_rover_routes().observe(getViewLifecycleOwner(), routes -> {
            if (routes == null || routes.isEmpty()) {
                return;
            }

            List<List<GeoPoint>> drawable_routes = new ArrayList<>();
            for (RoverRoute rover_route: routes) {
                List<GeoPoint> route = rover_route.route;
                clear_route_edit_markers();
                edit_rover_route_markers = null;

                if (route != null && route.size() > 1) {
                    find_and_delete_overlay("polyline", true);

                    drawable_routes.add(route);
                }
            }

            List<List<Marker>> tmp = draw_routes(drawable_routes);
            if (tmp != null && !tmp.isEmpty()) {
                edit_rover_route_markers = tmp;
            }
        });

    }

    //TODO as soon as there is an own fragment for this add own view_binding
    protected FragmentMapBinding get_view_binding(View view) {
        return FragmentMapBinding.bind(view);
    }

    // TODO add own view model for this class
    //  (as soon as it is used for the map of the rover route)
    protected MapViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(MapViewModel.class);
    }

    // TODO override in FlightMapFragment to make sure all the specific things are in there
    protected void setPolygon(Polygon polygon) {

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

    protected void set_marker_selected(Marker new_selected) {
        selected_marker = new_selected;
        new_selected.setDraggable(true);
        Drawable selected_marker_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_selected, null);
        new_selected.setIcon(selected_marker_icon);
        new_selected.setAnchor(0.35f, 0.68f);
        view_binding.map.invalidate();
    }

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

    protected void clear_route_edit_markers() {
        if (edit_rover_route_markers != null && !edit_rover_route_markers.isEmpty()) {
            for (List<Marker> marker_route: edit_rover_route_markers) {
                view_binding.map.getOverlayManager().removeAll(marker_route);
            }
        }

    }

    protected void edit_route_button_activate() {
        if (!add_route_edit_markers()) return;

        view_binding.buttonEditRoute.setText("Stop Edit");
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
        view_binding.buttonAddMarker.setVisibility(View.VISIBLE);
        view_binding.buttonDeleteMarker.setVisibility(View.VISIBLE);
        view_binding.buttonPolygonEdit.setEnabled(false);
        current_state = VIEW_STATE.EDIT_ROUTE;
    }

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

            view_model.set_rover_routes(new_routes);
        }
    }

    protected void edit_route_button_deactivate() {
        clear_route_edit_markers();
        view_binding.buttonEditRoute.setText("Edit Route");
        view_binding.buttonAddMarker.setVisibility(View.INVISIBLE);
        view_binding.buttonDeleteMarker.setVisibility(View.INVISIBLE);

        save_route_or_routes();

        current_state = VIEW_STATE.NONE;
        set_marker_unselected(true);
    }

    protected void set_click_listeners() {
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



    protected void delete_marker_route_edit() {
        for (List<Marker> marker_route: edit_rover_route_markers) {
            if (marker_route.contains(selected_marker)) {
                marker_route.remove(selected_marker);
                view_binding.map.getOverlayManager().remove(selected_marker);
                set_marker_unselected(true);
                changed_during_edit = true;
                view_binding.map.invalidate();
                break;
            }
        }
    }

    protected void delete_marker_polygon_edit() {}

    protected void add_marker_route_edit() {
        Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(),
                true,
                false);

        for (int i = 0; i < edit_rover_route_markers.size(); i++) {
            List<Marker> marker_route = edit_rover_route_markers.get(i);
            if (marker_route.contains(selected_marker)) {
                edit_rover_route_markers.set(i,
                        insert_marker_at_index(marker_route,
                                marker_route.indexOf(selected_marker),
                                new_marker));
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

    protected void configure_buttons() {
        view_binding.buttonPolygonEdit.setEnabled(false);
        view_binding.buttonPolygonEdit.setVisibility(View.INVISIBLE);
        view_binding.buttonExportPolygonToKml.setEnabled(false);
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
    }

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
