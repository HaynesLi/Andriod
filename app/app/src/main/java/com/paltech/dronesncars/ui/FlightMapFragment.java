package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FlightMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class FlightMapFragment extends MapFragment {

    private List<Marker> polygon_vertices;
    private List<List<Marker>> polygon_holes;

    private boolean initial_polygon_edit;


    public FlightMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static FlightMapFragment newInstance() {
        FlightMapFragment fragment = new FlightMapFragment();
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

        initial_polygon_edit = false;

        view_binding.buttonPolygonEdit.setText("Edit Polygon");

        getArgsFromParent();

    }

    @Override
    protected FragmentMapBinding get_view_binding(View view) {
        return FragmentMapBinding.bind(view);
    }

    @Override
    protected MapViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(MapViewModel.class);
    }

    protected void set_edit_buttons_enabled(boolean enabled) {
        super.set_edit_buttons_enabled(enabled);
        view_binding.buttonPolygonEdit.setEnabled(enabled);
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
        set_center_to_last_location();
        start_polygon_edit();
    }

    private void start_polygon_edit() {
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
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
        view_binding.buttonExportPolygonToKml.setVisibility(View.VISIBLE);
        view_binding.map.invalidate();
        current_state = VIEW_STATE.NONE;
    }

    private void parseKMLFile(Uri kml_file_uri) {
        view_model.parseKMLFile(kml_file_uri);
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

    @Override
    protected void set_click_listeners() {
        super.set_click_listeners();
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
    }







    @Override
    protected void configureMap() {
        super.configureMap();
        OverlayManager overlay_manager = view_binding.map.getOverlayManager();
        overlay_manager.removeAll(overlay_manager.overlays());

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

    }

    @Override
    protected void setLiveDataSources() {
        super.setLiveDataSources();

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

        view_model.getRoute().observe(getViewLifecycleOwner(), flight_route -> {
            if (flight_route != null) {
                List<GeoPoint> route = flight_route.route;
                if (edit_route_markers != null && edit_route_markers.size() != 0) {
                    view_binding.map.getOverlayManager().removeAll(edit_route_markers);
                    edit_route_markers = null;
                }
                if (route != null && route.size() > 1) {

                    // remove the old route drawing (if there was one)
                    find_and_delete_overlay("polyline", false);

                    List<List<Marker>> tmp = draw_routes(new ArrayList<>(Collections.singletonList(route)));
                    if (tmp != null && tmp.size() >= 1) {
                        edit_route_markers = tmp.get(0);
                    }
                }
            }
        });
    }

    @Override
    protected void delete_marker_polygon_edit() {
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

    @Override
    protected void add_marker_polygon_edit() {
        if (current_state == VIEW_STATE.EDIT_ROUTE) {
            add_marker_route_edit();
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

    @Override
    protected void polygon_to_markers(Polygon polygon) {
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
    }

    @Override
    protected void configure_buttons() {}

    @Override
    protected void edit_route_button_activate() {
        super.edit_route_button_activate();
    }

    @Override
    protected void edit_route_button_deactivate() {
        super.edit_route_button_deactivate();
        view_binding.buttonExportPolygonToKml.setVisibility(View.VISIBLE);
        view_binding.buttonPolygonEdit.setEnabled(true);
    }

}