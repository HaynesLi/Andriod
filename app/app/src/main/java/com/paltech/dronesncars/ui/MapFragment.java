package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class MapFragment extends LandscapeFragment {

    private FragmentMapBinding view_binding;
    private MapViewModel view_model;
    private List<Marker> edit_route_markers;
    private List<Marker> polygon_vertices;
    private List<List<Marker>> polygon_holes;

    private enum VIEW_STATE {NONE, EDIT_POLYGON, EDIT_ROUTE}

    private boolean is_flight_map;
    private VIEW_STATE current_state;
    private boolean changed_during_edit;
    private boolean initial_polygon_edit;
    private Marker selected_marker = null;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentMapBinding.bind(view);
        view_model = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        is_flight_map = get_is_flight_map();
        current_state = VIEW_STATE.NONE;
        changed_during_edit = false;
        edit_route_markers = null;
        initial_polygon_edit = false;

        view_binding.buttonEditRoute.setText("Edit Route");
        view_binding.buttonPolygonEdit.setText("Edit Polygon");

        configureMap();
        setLiveDataSources();
        getArgsFromParent();
        set_click_listeners();
    }

    private boolean get_is_flight_map() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment.getClass() == DroneScreen.class) {
            return true;
        } else if (parentFragment != null && parentFragment.getClass() == RoverRouteFragment.class) {
            return false;
        }
        return false;
    }

    private void setLiveDataSources() {

        // watch dictionary of String (FID) and osmdroid.(...).Polygon in order to, if necessary,
        // display an AlertDialog to make the user chose one of the available polygons
        view_model.choosePolygonFromKML.observe(getViewLifecycleOwner(), stringPolygonDictionary -> {
            if (!stringPolygonDictionary.isEmpty()) {
                String[] selectableNames = new String[stringPolygonDictionary.size()];
                selectableNames = Collections.list(stringPolygonDictionary.keys()).toArray(selectableNames);
                if (stringPolygonDictionary.size() > 1) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                    dialogBuilder.setTitle("Pick a Polygon");
                    String[] finalSelectableNames = selectableNames;
                    dialogBuilder.setItems(selectableNames, (dialog, which) -> {
                        String clickedFID = finalSelectableNames[which];
                        view_model.setPolygon(stringPolygonDictionary.get(clickedFID));
                    });
                    dialogBuilder.show();
                } else {
                    view_model.setPolygon(stringPolygonDictionary.get(selectableNames[0]));
                }
            }
        });

        // draw the "new" polygon if it changed in the database
        view_model.polygon.observe(getViewLifecycleOwner(), polygon -> {
            if (polygon != null) {
                setPolygon(polygon);
                view_binding.buttonEditRoute.setEnabled(true);
                view_binding.buttonPolygonEdit.setEnabled(true);
            } else {
                find_and_delete_overlay("both");
            }

        });

        if (is_flight_map) {
            view_model.getRoute().observe(getViewLifecycleOwner(), flight_route -> {
                if (flight_route != null) {
                    List<GeoPoint> route = flight_route.route;
                    if (edit_route_markers != null && edit_route_markers.size() != 0) {
                        view_binding.map.getOverlayManager().removeAll(edit_route_markers);
                        edit_route_markers = null;
                    }
                    if (route != null && route.size() > 1) {

                        // remove the old route drawing (if there was one)
                        find_and_delete_overlay("polyline");

                        for (GeoPoint point: route) {
                            if (edit_route_markers == null) {
                                edit_route_markers = new ArrayList<>();
                            }
                            edit_route_markers.add(build_edit_marker(point, true, false));
                        }

                        // add the new one
                        Polyline route_drawable_overlay = new Polyline();
                        route_drawable_overlay.setPoints(route);
                        route_drawable_overlay.getOutlinePaint().setStrokeWidth(1);

                        view_binding.map.getOverlayManager().add(route_drawable_overlay);
                        view_binding.map.invalidate();
                    }
                }
            });
        }
    }

    private void getArgsFromParent() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment.getClass() == DroneScreen.class) {
            DroneScreen parentDroneScreen = (DroneScreen) parentFragment;
            Uri kml_file_uri = parentDroneScreen.getKml_file_uri();
            if (kml_file_uri != null) {
                parseKMLFile(kml_file_uri);
            } else {
                initial_polygon_edit();
            }
        }
    }

    private void initial_polygon_edit() {
        initial_polygon_edit = true;
        start_polygon_edit();
    }

    private void start_polygon_edit() {
        view_binding.buttonEditRoute.setEnabled(false);
        view_binding.buttonPolygonEdit.setEnabled(true);
        view_binding.buttonPolygonEdit.setText("stop edit");
        view_binding.map.invalidate();
        current_state = VIEW_STATE.EDIT_POLYGON;
    }

    private void stop_polygon_edit() {
        if (polygon_vertices != null) {view_binding.map.getOverlayManager().removeAll(polygon_vertices);}
        if (polygon_holes != null) {
            for (List<Marker> hole: polygon_holes) {
                view_binding.map.getOverlayManager().removeAll(hole);
            }
        }
        polygon_vertices = null;
        polygon_holes = null;
        view_binding.buttonPolygonEdit.setText("edit polygon");
        view_binding.buttonPolygonEdit.setEnabled(false);
        view_binding.buttonEditRoute.setEnabled(true);
        view_binding.map.invalidate();
        current_state = VIEW_STATE.NONE;
    }

    private void parseKMLFile(Uri kml_file_uri) {
        view_model.parseKMLFile(kml_file_uri);
    }


    private void configureMap() {
        Context ctx = requireContext();
        // ATTENTION: this configuration is NOT part of android, but of osmdroid!
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // TODO what is this? use offline instead
        // TODO remove previously drawn route if the polygon is a different one
        // TODO check route for plausibility
        OverlayManager overlayManager = view_binding.map.getOverlayManager();
        overlayManager.removeAll(overlayManager.overlays());
        view_binding.map.invalidate();

        view_binding.map.setTileSource(TileSourceFactory.MAPNIK);

        view_binding.map.setMultiTouchControls(true);

        view_model.clearPolygon();
        view_model.clearSelectablePolygons();



        final MapEventsReceiver map_events_receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (current_state == VIEW_STATE.EDIT_POLYGON && initial_polygon_edit) {
                    Marker tmp_marker = build_edit_marker(p, false, true);
                    if (polygon_vertices == null) {
                        polygon_vertices = new ArrayList<>();
                    }
                    polygon_vertices.add(tmp_marker);
                    view_binding.map.getOverlayManager().add(tmp_marker);
                    view_binding.map.invalidate();
                }

                return current_state == VIEW_STATE.EDIT_POLYGON && initial_polygon_edit;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        view_binding.map.getOverlays().add(new MapEventsOverlay(map_events_receiver));
        view_binding.map.invalidate();

        IMapController mapController = view_binding.map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(48.17808437657652, 11.795518397832884);

        mapController.setCenter(startPoint);


    }

    private void setPolygon(Polygon polygon) {

        polygon.getFillPaint().setColor(Color.parseColor("#1EFFE70E"));

        if (polygon.getActualPoints() != null && polygon.getActualPoints().size() > 0) {
            polygon_vertices = new ArrayList<>();
            polygon_holes = new ArrayList<>();
            for (GeoPoint polygon_vertex: polygon.getActualPoints()) {
                polygon_vertices.add(build_edit_marker(polygon_vertex, true, false));
            }
            for (List<GeoPoint> hole: polygon.getHoles()) {
                List<Marker> current_hole = new ArrayList<>();
                for (GeoPoint hole_vertex: hole) {
                    current_hole.add(build_edit_marker(hole_vertex, true, false));
                }
                polygon_holes.add(current_hole);
            }


            view_binding.map.getOverlayManager().removeAll(view_binding.map.getOverlays());
            view_binding.map.getOverlayManager().add(polygon);
            view_binding.map.invalidate();

            IMapController mapController = view_binding.map.getController();
            mapController.setCenter(polygon.getActualPoints().get(0));
            mapController.setZoom(16.0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        view_binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        view_binding.map.onPause();
    }

    private void find_and_delete_overlay(String type) {
        List<Overlay> overlays = view_binding.map.getOverlayManager().overlays();
        label:
        for (Overlay overlay : overlays) {
            switch (type) {
                case "polyline":
                    if (overlay.getClass() == Polyline.class) {
                        view_binding.map.getOverlayManager().remove(overlay);
                        break label;
                    }
                    break;
                case "polygon":
                    if (overlay.getClass() == Polygon.class) {
                        view_binding.map.getOverlayManager().remove(overlay);
                        break label;
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

    private void set_click_listeners() {
        view_binding.buttonEditRoute.setOnClickListener(v -> {
            if (current_state == VIEW_STATE.NONE) {
                view_binding.map.getOverlayManager().addAll(edit_route_markers);
                view_binding.buttonEditRoute.setText("Stop Edit");
                view_binding.buttonAddMarker.setVisibility(View.VISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.VISIBLE);
                view_binding.buttonPolygonEdit.setEnabled(false);
                current_state = VIEW_STATE.EDIT_ROUTE;
            } else if (current_state == VIEW_STATE.EDIT_ROUTE){
                view_binding.map.getOverlayManager().removeAll(edit_route_markers);
                view_binding.buttonEditRoute.setText("Edit Route");
                view_binding.buttonAddMarker.setVisibility(View.INVISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.INVISIBLE);
                if (changed_during_edit) {
                    changed_during_edit = false;
                    List<GeoPoint> new_route = new ArrayList<>();
                    for (Marker marker: edit_route_markers){
                        new_route.add(marker.getPosition());
                    }

                    view_model.set_flight_route(new_route);
                }
                view_binding.buttonPolygonEdit.setEnabled(true);
                current_state = VIEW_STATE.NONE;
                set_marker_unselected(true);
            }
            view_binding.map.invalidate();
        });

        view_binding.buttonPolygonEdit.setOnClickListener(v -> {
            if (current_state == VIEW_STATE.EDIT_POLYGON) {
                Polygon self_selected_polygon = new Polygon();
                if (polygon_vertices != null) {
                    for (Marker self_set_marker : polygon_vertices) {
                        self_selected_polygon.addPoint(self_set_marker.getPosition());
                    }
                }
                List<List<GeoPoint>> new_holes = new ArrayList<>();
                if (polygon_holes != null) {
                    for (List<Marker> hole: polygon_holes) {
                        List<GeoPoint> current_hole = new ArrayList<>();
                        for (Marker self_set_marker: hole) {
                            current_hole.add(self_set_marker.getPosition());
                        }
                        new_holes.add(current_hole);
                    }
                }
                self_selected_polygon.setHoles(new_holes);

                set_marker_unselected(true);

                stop_polygon_edit();
                view_binding.buttonAddMarker.setVisibility(View.INVISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.INVISIBLE);
                initial_polygon_edit = false;
                view_model.setPolygon(self_selected_polygon);
            } else if (current_state == VIEW_STATE.NONE && polygon_vertices != null) {
                view_binding.buttonAddMarker.setVisibility(View.VISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.VISIBLE);

                start_polygon_edit();

                view_binding.map.getOverlayManager().addAll(polygon_vertices);
                if (polygon_holes != null) {
                    for (List<Marker> hole : polygon_holes) {
                        view_binding.map.getOverlayManager().addAll(hole);
                    }
                }

                view_binding.map.invalidate();
            }
        });

        view_binding.buttonDeleteMarker.setOnClickListener(v -> {
            if (selected_marker != null) {
                if (current_state == VIEW_STATE.EDIT_ROUTE) {
                    edit_route_markers.remove(selected_marker);
                    view_binding.map.getOverlayManager().remove(selected_marker);
                    set_marker_unselected(true);
                    // TODO do we really need this? seems overkill to me... Or if not, do we need it
                    //  for current_state == VIEW_STATE.EDIT_POLYGON, too?
                    changed_during_edit = true;
                    view_binding.map.invalidate();
                } else if (current_state == VIEW_STATE.EDIT_POLYGON) {
                    if (!polygon_vertices.remove(selected_marker)) {
                        for (List<Marker> hole : polygon_holes) {
                            if (hole.remove(selected_marker)) {
                                break;
                            }
                        }
                    }
                    view_binding.map.getOverlayManager().remove(selected_marker);
                    set_marker_unselected(true);
                    view_binding.map.invalidate();
                }
            }
        });

        view_binding.buttonAddMarker.setOnClickListener(v -> {
            if (selected_marker != null) {
                if (current_state == VIEW_STATE.EDIT_ROUTE) {
                    Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(), true, false);
                    edit_route_markers = insert_marker_at_index(edit_route_markers, edit_route_markers.indexOf(selected_marker), new_marker);
                    set_marker_selected(new_marker);
                    view_binding.map.getOverlayManager().add(new_marker);
                    view_binding.map.invalidate();
                    changed_during_edit = true;
                } else if (current_state == VIEW_STATE.EDIT_POLYGON) {
                    Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(), true, false);
                    if (polygon_vertices.contains(selected_marker)) {
                        polygon_vertices = insert_marker_at_index(polygon_vertices, polygon_vertices.indexOf(selected_marker), new_marker);
                    } else {
                        for (int i = 0; i < polygon_holes.size(); i++) {
                            List<Marker> hole = polygon_holes.get(i);
                            if (hole.contains(selected_marker)) {
                                polygon_holes.set(i, insert_marker_at_index(hole, hole.indexOf(selected_marker), new_marker));
                                break;
                            }
                        }
                    }
                    set_marker_selected(new_marker);
                    view_binding.map.getOverlayManager().add(new_marker);
                    view_binding.map.invalidate();
                }
            }
        });
    }

    private void set_marker_selected(Marker new_selected) {
        selected_marker = new_selected;
        new_selected.setDraggable(true);
        Drawable selected_marker_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_selected, null);
        new_selected.setIcon(selected_marker_icon);
        new_selected.setAnchor(0.35f, 0.68f);
        view_binding.map.invalidate();
    }

    private void set_marker_unselected(boolean set_null) {
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

    private List<Marker> insert_marker_at_index(List<Marker> list, int index, Marker marker) {
        if (index > 0 && index <= list.size()) {
            List<Marker> before = list.subList(0, index);
            List<Marker> after = list.subList(index, list.size());
            List<Marker> insertion = new ArrayList<>();
            insertion.add(marker);
            return Stream.of(before, insertion, after).flatMap(Collection::stream).collect(Collectors.toList());
        }
        return null;
    }

    private Marker build_edit_marker(GeoPoint geoPoint, boolean changed_during_edit_on_drag, boolean draggable_default) {
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

}