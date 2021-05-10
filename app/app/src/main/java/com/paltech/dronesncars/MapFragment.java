package com.paltech.dronesncars;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

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