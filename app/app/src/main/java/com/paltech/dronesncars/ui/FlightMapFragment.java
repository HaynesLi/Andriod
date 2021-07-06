package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
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
    private List<Marker> edit_route_markers;

    private final ActivityResultLauncher<String> getKML = registerForActivityResult(new ActivityResultContracts.GetContent(),
            this::use_kml_result);


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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {


        super.onViewCreated(view, savedInstanceState);

        view_binding.buttonImportKml.setVisibility(View.VISIBLE);
        view_binding.buttonPolygonEdit.setVisibility(View.VISIBLE);
        view_binding.buttonPolygonEdit.setEnabled(true);
        view_binding.buttonDeletePolygon.setVisibility(View.VISIBLE);

        view_binding.buttonPolygonEdit.setText("Edit Polygon");

        //getArgsFromParent();

    }

    @Override
    protected boolean add_route_edit_markers() {
        if(edit_route_markers != null) {
            view_binding.map.getOverlayManager().addAll(edit_route_markers);
            return true;
        } else  {
            return false;
        }
    }

    @Override
    protected void clear_route_edit_markers() {
        if (edit_route_markers != null && !edit_route_markers.isEmpty()) {
            view_binding.map.getOverlayManager().removeAll(edit_route_markers);
        }
    }

    @Override
    protected void save_route_or_routes() {
        if (changed_during_edit) {
            changed_during_edit = false;
            List<GeoPoint> new_route = new ArrayList<>();
            for (Marker marker: edit_route_markers){
                new_route.add(marker.getPosition());
            }

            view_model.set_flight_route(new_route);
        }
    }

    @Override
    protected void different_inits() {
        edit_route_markers = null;
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


    private void start_polygon_edit() {
        view_binding.buttonExportPolygonToKml.setVisibility(View.INVISIBLE);
        view_binding.buttonEditRoute.setEnabled(false);
        view_binding.buttonPolygonEdit.setEnabled(true);
        view_binding.buttonPolygonEdit.setText("stop edit");
        view_binding.buttonDeletePolygon.setEnabled(false);

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
        view_binding.buttonPolygonEdit.setEnabled(true);
        view_binding.buttonEditRoute.setEnabled(true);
        view_binding.buttonExportPolygonToKml.setVisibility(View.VISIBLE);
        view_binding.buttonDeletePolygon.setEnabled(true);
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
                if (polygon_vertices != null && polygon_vertices.size() >= 3) {
                    Polygon self_selected_polygon = new Polygon();
                    for (Marker self_set_marker : polygon_vertices) {
                        self_selected_polygon.addPoint(self_set_marker.getPosition());
                    }
                    List<List<GeoPoint>> new_holes = new ArrayList<>();
                    if (polygon_holes != null) {
                        for (List<Marker> hole : polygon_holes) {
                            List<GeoPoint> current_hole = new ArrayList<>();
                            for (Marker self_set_marker : hole) {
                                current_hole.add(self_set_marker.getPosition());
                            }
                            new_holes.add(current_hole);
                        }
                    }
                    self_selected_polygon.setHoles(new_holes);

                    view_model.setPolygon(self_selected_polygon);
                } else {
                    view_model.clearPolygon();
                }

                set_marker_unselected(true);
                stop_polygon_edit();
                view_binding.buttonAddMarker.setVisibility(View.INVISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.INVISIBLE);

            } else if (current_state == VIEW_STATE.NONE) {
                view_binding.buttonAddMarker.setVisibility(View.VISIBLE);
                view_binding.buttonDeleteMarker.setVisibility(View.VISIBLE);


                start_polygon_edit();
                if (polygon_vertices != null && !polygon_vertices.isEmpty()) {
                    view_binding.map.getOverlayManager().addAll(polygon_vertices);
                    if (polygon_holes != null) {
                        for (List<Marker> hole : polygon_holes) {
                            view_binding.map.getOverlayManager().addAll(hole);
                        }
                    }
                }

                view_binding.map.invalidate();
            }
        });

        view_binding.buttonImportKml.setOnClickListener(v -> getKML.launch("application/vnd.google-earth.kml+xml"));

        view_binding.buttonDeletePolygon.setOnClickListener(v -> {
            view_model.clearPolygon();
            polygon_vertices = null;
            polygon_holes = null;
        });
    }







    @Override
    protected void configureMap() {
        super.configureMap();

        view_model.clearSelectablePolygons();
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
        if (polygon_vertices == null) {
            polygon_vertices = new ArrayList<>();
        }
            Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(), true, false);
            if (selected_marker == null && (polygon_vertices == null || polygon_vertices.isEmpty())) {
                if (polygon_vertices == null) {
                    polygon_vertices = new ArrayList<>();
                }
                polygon_vertices.add(new_marker);
            }
            else if (polygon_vertices.contains(selected_marker)) {
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

    @Override
    protected void observe_rover_routes(){}

    @Override
    protected void delete_marker_route_edit() {
        edit_route_markers.remove(selected_marker);
        view_binding.map.getOverlayManager().remove(selected_marker);
        set_marker_unselected(true);
        // TODO do we really need this? seems overkill to me... Or if not, do we need it
        //  for current_state == VIEW_STATE.EDIT_POLYGON, too?
        changed_during_edit = true;
        view_binding.map.invalidate();
    }

    @Override
    protected void add_marker_route_edit() {
        Marker new_marker = build_edit_marker((GeoPoint) view_binding.map.getMapCenter(),
                true,
                false);
        edit_route_markers = insert_marker_at_index(edit_route_markers, edit_route_markers.indexOf(selected_marker), new_marker);
        set_marker_unselected(false);
        set_marker_selected(new_marker);
        view_binding.map.getOverlayManager().add(new_marker);
        view_binding.map.invalidate();
        changed_during_edit = true;
    }

    // TODO is this necessary? or is it possible to just pass the Uri directly to parseKMLFile(...),
    //  (no null-check required?)
    private void use_kml_result(Uri kml_file_uri) {
        if (kml_file_uri != null) {
            parseKMLFile(kml_file_uri);
        }
    }
}