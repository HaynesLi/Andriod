package com.paltech.dronesncars.computing;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.paltech.dronesncars.R;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class XMLParser {

    static int width_result_view, height_result_view, width_image, height_image;
    static float scale_width_image_result, scale_height_image_result;

    public static void setResultViewWidth(int width) {
        width_result_view = width;
    }
    public static void setResultViewHeight(int height) {
        height_result_view = height;
    }

    public static float getScale_width_image_result() {
        return scale_width_image_result;
    }

    public static float getScale_height_image_result() {
        return scale_height_image_result;
    }

    /**
     * get an InputStream for a specific Uri
     *
     * @param context - the applications context
     * @param uri     - the Uri (Android) to open an input stream to
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

    public static OutputStream getOutputStreamByUri(Context context, Uri uri) {
        try {
            return context.getContentResolver().openOutputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseFullXml(Context context, Uri uri_xml) {
        FileInputStream fileInputStream = (FileInputStream) getInputStreamByUri(context, uri_xml);
        try {
//            byte[] b = new byte[fileInputStream.available()];
//            fileInputStream.read();
//            fileInputStream.close();
//            String str = new String(b,"UTF-8");
//            Log.e("","str" + str);
//            return str;
            byte[] buff = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int len = 0;
            while ((len = fileInputStream.read(buff)) > 0) {
                sb.append(new String(buff, 0, len));
            }
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor cursor = context.getContentResolver().query(uri_xml, null, null, null, null);

        return "";
//        return fileInputStream.toString();
    }


    /**
     * parse a KML File at the given Uri and return a dictionary that contains polygons and their
     * FIDs, which were found inside the KML file.
     * The current implementation of this method is very static and specific for our example file,
     * but of course could be replaced with a more sophisticated, general method
     *
     * @param uri_xml the uri of the XML file to parse
     * @param context the applications context
     * @return dictionary that contains polygons and their FIDs, which were found inside the KML
     * file
     */
    public static ArrayList<ScanResult> parseXMLFile(Uri uri_xml, Context context) {

//        String path_uri_xml = uri_xml.toString();
//        String xmlPath = path_uri_xml.substring(0, path_uri_xml.lastIndexOf("/"));
//        String xmlName = path_uri_xml.substring(xmlPath.lastIndexOf("/") + 1);
//        File xmlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), path_uri_xml);
//        InputStream fis = null;
//        InputStream inputStreamByUri = getInputStreamByUri(context, uri_xml);
//        try {
////            fis =context.getContentResolver().openInputStream(Uri.parse(path_uri_xml));
//            fis =context.getContentResolver().openInputStream(uri_xml);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        Log.e("xmlparser", "xmlfile" + xmlFile.exists());
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(xmlFile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        XmlPullParserFactory parserFactory;
//        try {
//            parserFactory = XmlPullParserFactory.newInstance();
//            XmlPullParser parser = parserFactory.newPullParser();
////            Log.e("xml", "" + fileInputStream.toString());
//
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(inputStreamByUri, null);
//            processParsing(parser);
//            fileInputStream.close();
//        } catch (IOException | XmlPullParserException e) {
//            e.printStackTrace();
//        }


        ArrayList<ScanResult> scanResultList = new ArrayList<>();
        FileInputStream fileInputStream = (FileInputStream) getInputStreamByUri(context, uri_xml);
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
//            Log.e("xml", "" + fileInputStream.toString());

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fileInputStream, null);
            scanResultList = processParsing(parser);
            fileInputStream.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        return scanResultList;
    }


    private static ArrayList<ScanResult> processParsing(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<ScanResult> scanResultList = new ArrayList<>();
        int eventType = parser.getEventType();
        int left = 0, top = 0, right = 0, bottom = 0;
        String className = "Weed";
        float confidence = 1f;


        while (eventType != XmlPullParser.END_DOCUMENT) {
            String eltName = null;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    eltName = parser.getName();
                    if (eltName.equals("width")) {
                        width_image = Integer.parseInt((parser.nextText()));
                        scale_width_image_result = ((float) width_result_view) / ((float) width_image);
                    } else if (eltName.equals("height")) {
                        height_image = Integer.parseInt(parser.nextText());
                        scale_height_image_result = ((float) height_result_view) / ((float) height_image);
                    } else if (eltName.equals("bndbox")) {

                    } else if (eltName.equals("className")) {
                        className = parser.nextText();
                    } else if (eltName.equals("confidence")) {
                        confidence = Float.parseFloat(parser.nextText());
                    } else if (eltName.equals("xmin")) {
                        left = (int) (Integer.parseInt(parser.nextText()) * scale_width_image_result);
                    } else if (eltName.equals("ymin")) {
                        top = (int) (Integer.parseInt(parser.nextText()) * scale_height_image_result);
                    } else if (eltName.equals("xmax")) {
                        right = (int) (Integer.parseInt(parser.nextText()) * scale_width_image_result);
                    } else if (eltName.equals("ymax")) {
                        bottom = (int) (Integer.parseInt(parser.nextText()) * scale_height_image_result);
                        ScanResult currentResult = new ScanResult(className,confidence,new Rect(left,top,right,bottom));
                        scanResultList.add(currentResult);
                    }
                    break;
            }
            eventType = parser.next();
        }
        return scanResultList;
    }

}

