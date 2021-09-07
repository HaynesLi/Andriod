package com.paltech.dronesncars.computing;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class XMLParser {
    /**
     * get an InputStream for a specific Uri
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

    /**
     * parse a KML File at the given Uri and return a dictionary that contains polygons and their
     * FIDs, which were found inside the KML file.
     * The current implementation of this method is very static and specific for our example file,
     * but of course could be replaced with a more sophisticated, general method
     * @param xml_file_uri the uri of the KML file to parse
     * @param context the applications context
     * @return dictionary that contains polygons and their FIDs, which were found inside the KML
     * file
     */
    public static ArrayList<double[]> parseXMLFile(String xml_file_uri, Context context) {

        String uriPath_Name =xml_file_uri.toString();
        String xmlPath = uriPath_Name.substring(0, uriPath_Name.lastIndexOf("/"));
        String xmlName = uriPath_Name.substring(xmlPath.lastIndexOf("/") + 1);
        File xmlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),uriPath_Name);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
//            Log.e("xml", "" + fileInputStream.toString());

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fileInputStream, null);
            processParsing(parser);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        double[] bBox1 = new double[4];
        bBox1[0] = 0.3;
        bBox1[1] = 0.16;
        bBox1[2] = 0.44;
        bBox1[3] = 0.32;
        ArrayList<double[]> bBoxList = new ArrayList<>();
        bBoxList.add(bBox1);

        return bBoxList;
    }



    private static ArrayList<double[]> processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<double[]> bBoxList = new ArrayList<>();
        int eventType = parser.getEventType();
        double[] current = null;
        double wid = 0, hgh = 0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if (eltName.equals("width")) {
                        wid = Double.parseDouble(parser.nextText());

                    } else if (eltName.equals("height")) {
                        hgh = Double.parseDouble(parser.nextText());
                    } else if (eltName.equals("bndbox")) {
                        current = new double[4];
                    } else if (eltName.equals("xmin")) {
                        current[0] = Integer.parseInt(parser.nextText()) / wid;
                    } else if (eltName.equals("ymin")) {
                        current[1] = Integer.parseInt(parser.nextText()) / hgh;
                    } else if (eltName.equals("xmax")) {
                        current[2] = Integer.parseInt(parser.nextText()) / wid;
                    } else if (eltName.equals("ymax")) {
                        current[3] = Integer.parseInt(parser.nextText()) / hgh;
                        bBoxList.add(current);
                    }

                    break;
            }
            eventType = parser.next();
        }
        Log.e("width", "" + (wid == 0 ? "" : wid));

        Log.e("height", "" + (hgh == 0 ? "" : hgh));
        Log.e("bboxlist", "" + bBoxList.toString());
        return bBoxList;
    }

    /**
     * get the KMLDocument for a given polygon, which can be saved in its own file
     * the current implementation tries to copy the given example-files structure, but does not
     * enter meaningful values into the different extended-data fields.
     * @param polygon the polygon to get the kml file for
     * @return the KmlDocument
     */

}

