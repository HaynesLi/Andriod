package com.paltech.dronesncars.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private FragmentMapBinding view_binding;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance(String param1, String param2) {
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

        configureMap();
    }

    private void configureMap() {
        Context ctx = requireContext();
        // ATTENTION: this configuration is NOT part of android, but of osmdroid!
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //TODO what is this? use offline instead
        view_binding.map.setTileSource(TileSourceFactory.MAPNIK);

        view_binding.map.setMultiTouchControls(true);

        IMapController mapController = view_binding.map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(48.17808437657652, 11.795518397832884);

        mapController.setCenter(startPoint);

        setPolygon(new Polygon());
    }

    // TODO: still a mock. replace it with something proper
    private void setPolygon(Polygon polygon) {
        Polygon mock_polygon = new Polygon();
        List<GeoPoint> geoPointList = new ArrayList<>();
        geoPointList.add(new GeoPoint(48.29574258901285, 11.896900532799023));
        geoPointList.add(new GeoPoint(48.30841764645962, 11.917242405117028));
        geoPointList.add(new GeoPoint(48.312927380430466, 11.894068121549093));
        mock_polygon.getFillPaint().setColor(Color.parseColor("#1EFFE70E"));
        mock_polygon.setPoints(geoPointList);
        mock_polygon.setTitle("I am a mock polygon");

        view_binding.map.getOverlayManager().add(mock_polygon);

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


}