package com.paltech.dronesncars.model;

import android.content.Context;
import android.net.Uri;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.InputStream;
import java.util.Dictionary;
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
}
