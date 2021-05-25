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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.computing.TestPointInPolygon;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.Collections;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class MapFragment extends Fragment {

    private FragmentMapBinding view_binding;
    private MapViewModel view_model;

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

        configureMap();
        setLiveDataSources();
        getArgsFromParent();
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
    }

    private void getArgsFromParent() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment.getClass() == DroneScreen.class) {
            DroneScreen parentDroneScreen = (DroneScreen) parentFragment;
            Uri kml_file_uri = parentDroneScreen.getKml_file_uri();
            if (kml_file_uri != null) {
                Toast.makeText(getContext(),
                        String.format("Got Uri: %s", kml_file_uri.toString()),
                        Toast.LENGTH_SHORT).show();
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

        //TODO what is this? use offline instead
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
            view_binding.map.getOverlayManager().add(polygon);

            // Test code to test the point generation for the flight route
            // TODO clean code & put the computation in the background
            /*TestPointInPolygon test = new TestPointInPolygon();
            GeoPoint[][] somepoints = test.checkPoints(polygon, 0.01);

            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            items.add(new OverlayItem("", "", somepoints[0][0]));
            items.add(new OverlayItem("", "", somepoints[0][somepoints[0].length-1]));
            items.add(new OverlayItem("", "", somepoints[somepoints.length-1][0]));
            items.add(new OverlayItem("", "", somepoints[somepoints.length-1][somepoints[0].length-1]));

            for (GeoPoint[] pp : somepoints) {
                for (GeoPoint p : pp) {
                    if(p != null) {
                        items.add(new OverlayItem("", "", p));
                    }
                }
        }

            BoundingBox boundingBox = polygon.getBounds();
            GeoPoint north_east = new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonEast());
            GeoPoint north_west = new GeoPoint(boundingBox.getLatNorth(), boundingBox.getLonWest());
            GeoPoint south_east = new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonEast());
            GeoPoint south_west = new GeoPoint(boundingBox.getLatSouth(), boundingBox.getLonWest());

            items.add(new OverlayItem("north-east", "", north_east));
            items.add(new OverlayItem("north-west", "", north_west));
            items.add(new OverlayItem("south-east", "", south_east));
            items.add(new OverlayItem("south_west", "", south_west));


            ItemizedIconOverlay mOverlay = new ItemizedIconOverlay<OverlayItem>(getContext(), items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            //do something
                            return true;
                        }
                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    });



            view_binding.map.getOverlayManager().add(mOverlay);




            Marker startMarker = new Marker(view_binding.map);
            startMarker.setPosition(somepoints[0][0]);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            view_binding.map.getOverlays().add(startMarker);*/

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


}