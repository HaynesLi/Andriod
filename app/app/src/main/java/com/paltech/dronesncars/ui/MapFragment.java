package com.paltech.dronesncars.ui;

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
import com.paltech.dronesncars.databinding.FragmentMapBinding;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
        view_model = new ViewModelProvider(requireActivity()).get(MapViewModel.class);

        getArgsFromParent();
        configureMap();
    }

    /**
     *
     * @param context - the applications context
     * @param uri - the Uri (Android) to open an input stream to
     * @return - an InputStream to the given Uri
     */
    private static InputStream getInputStreamByUri(Context context, Uri uri) {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getArgsFromParent(){
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
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseKMLStream(getInputStreamByUri(getContext(), kml_file_uri), null);

        List<KmlFeature> placemarks = ((KmlFolder) kmlDocument.mKmlRoot.mItems.get(0)).mItems;
        Dictionary<String, Polygon> polygonDictionary = new Hashtable<>();
        for (KmlFeature placemark: placemarks) {
            if (placemark instanceof KmlPlacemark) {
                KmlPlacemark placemark_casted = (KmlPlacemark) placemark;
                if (placemark_casted.hasGeometry(KmlPolygon.class)) {
                    KmlPolygon kml_polygon = (KmlPolygon) placemark_casted.mGeometry;
                    String polygon_FID = placemark_casted.getExtendedData("FID");

                    Polygon polygon = new Polygon();
                    for (GeoPoint geoPoint : kml_polygon.mCoordinates) {
                        polygon.addPoint(geoPoint);
                    }
                    if (kml_polygon.mHoles != null && kml_polygon.mHoles.size() > 0) {
                        polygon.setHoles(kml_polygon.mHoles);
                    }

                    polygonDictionary.put(polygon_FID, polygon);
                }
            }
        }

        String x = "0" + kml_file_uri.toString();
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