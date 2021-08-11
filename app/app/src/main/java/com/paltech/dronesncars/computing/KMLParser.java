package com.paltech.dronesncars.computing;

import android.content.Context;
import android.net.Uri;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class KMLParser {
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

    public static Dictionary<String, Polygon> parseKMLFile(Uri kml_file_uri, Context context) {
        KmlDocument kmlDocument = new KmlDocument();
        kmlDocument.parseKMLStream(getInputStreamByUri(context, kml_file_uri), null);

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

        return polygonDictionary;
    }

    public static KmlDocument polygon_to_kml(Polygon polygon) {
        KmlDocument kmlDocument = new KmlDocument();
        KmlFolder new_folder = new KmlFolder();
        new_folder.mName = "Nutzung_0";

        KmlPlacemark kmlPlacemark = new KmlPlacemark();
        Style style = new Style();
        style.mLineStyle = new LineStyle();
        style.mLineStyle.mColor=0xff0000ff;
        //style.mPolyStyle = new ColorStyle();
        //style.mPolyStyle.mColorMode = 0; // TODO ist das hier fill = 0? => nein
        kmlPlacemark.mStyle = style.toString();

        HashMap<String, String> extended_data = new HashMap<>();
        extended_data.put("Betriebsnr", "0");
        extended_data.put("FID", "0");
        extended_data.put("FSNr", "0");
        extended_data.put("Schlag", "0");
        extended_data.put("Flaeche", "0");
        extended_data.put("Nutzung", "0");
        extended_data.put("Einstufung", "DG");
        extended_data.put("GPS", "nein");
        extended_data.put("BJS", "nein");

        kmlPlacemark.mExtendedData = extended_data;

        KmlPolygon kmlPolygon = new KmlPolygon();

        kmlPolygon.mCoordinates = new ArrayList<>(polygon.getActualPoints());
        if (polygon.getHoles() != null && !polygon.getHoles().isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> holes = new ArrayList<>(); // TODO stream().map() might come in handy here in theory

            for (List<GeoPoint> hole: polygon.getHoles()) {
                holes.add(new ArrayList<>(hole));
            }

            kmlPolygon.mHoles = holes;
        }

        kmlPlacemark.mGeometry = kmlPolygon;

        new_folder.add(kmlPlacemark);


        kmlDocument.mKmlRoot.add(new_folder);

        return kmlDocument;
    }
}
