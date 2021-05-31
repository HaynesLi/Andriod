package com.paltech.dronesncars.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private boolean is_flight_map;

    private boolean in_edit_state;

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
        in_edit_state = false;

        view_binding.buttonTriggerEditState.setText("Edit Route");

        configureMap();
        setLiveDataSources();
        getArgsFromParent();
        setClickListeners();
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
            } else {
                OverlayManager overlayManager = view_binding.map.getOverlayManager();
                overlayManager.removeAll(overlayManager.overlays());
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
                        List<Overlay> overlays = view_binding.map.getOverlayManager().overlays();
                        for (Overlay overlay : overlays) {
                            if (overlay.getClass() == Polyline.class) {
                                view_binding.map.getOverlayManager().remove(overlay);
                                break;
                            }
                        }

                        for (GeoPoint point: route) {
                            Marker tmp_marker = new Marker(view_binding.map, requireContext());
                            tmp_marker.setPosition(point);
                            tmp_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            if (edit_route_markers == null) {
                                edit_route_markers = new ArrayList<>();
                            }
                            edit_route_markers.add(tmp_marker);
                        }

                        // add the new one
                        Polyline route_drawable_overlay = new Polyline();
                        route_drawable_overlay.setPoints(route);

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
            }
        }
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


        IMapController mapController = view_binding.map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(48.17808437657652, 11.795518397832884);

        mapController.setCenter(startPoint);
    }

    private void setPolygon(Polygon polygon) {

        polygon.getFillPaint().setColor(Color.parseColor("#1EFFE70E"));

        if (polygon.getActualPoints() != null && polygon.getActualPoints().size() > 0) {
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
        String x = "x";
    }

    @Override
    public void onPause() {
        super.onPause();
        view_binding.map.onPause();
        String x = "x";
    }

    private void setClickListeners() {
        view_binding.buttonTriggerEditState.setOnClickListener(v -> {
            if (!in_edit_state) {
                view_binding.map.getOverlayManager().addAll(edit_route_markers);
                view_binding.buttonTriggerEditState.setText("Stop Edit");
            } else {
                view_binding.map.getOverlayManager().removeAll(edit_route_markers);
                view_binding.buttonTriggerEditState.setText("Edit Route");
            }
            view_binding.map.invalidate();
            in_edit_state = !in_edit_state;
        });
    }

}