package com.paltech.dronesncars.computing;


import android.net.Uri;

import com.paltech.dronesncars.model.Result;

import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * The interface which specifies the how a WeedDetector, or Computer-Vision-Pipeline has to interact
 * with the current implementation, or how a Mock has to look like. We only implemented mocks for
 * this part of the problem so it is of course possible to change this up if necessary
 */
public interface WeedDetectorInterface {
    /**
     * Method which takes a list if GPS-locations and a list of picture Uris, and returns a List of
     * Results == Locations where unwanted weed has been found.
     *
     * @param locations list of GPS-locations where a picture has been taken
     * @param pictures list of Uris to access the pictures from the phones storage.
     *                 Required: locations.size() == pictures.size() as each location gets paired
     *                 with one picture
     * @return list of Results, which contain a GPS location and a certainty
     */
    List<Result> get_results_from_pictures(List<GeoPoint> locations, List<Uri> pictures);

}
