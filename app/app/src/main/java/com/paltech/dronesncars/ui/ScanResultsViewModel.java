package com.paltech.dronesncars.ui;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.computing.ScanResult;
import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Result;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * The ViewModel used by {@link ScanResultsFragment}, which holds all the LiveData necessary to
 * update and display Computer-Vision-Scan results (which currently are completely mocked). A
 * subclass of {@link ViewModel}.
 */
@HiltViewModel
public class ScanResultsViewModel extends ViewModel {

    /**
     * The repository
     */
    private final Repository repository;

    @Inject
    public ScanResultsViewModel(Repository repository) {
        this.repository = repository;
    }

    /**
     * get a list of the scan results as LiveData
     *
     * @return the list of scan results as LiveData
     */
    public LiveData<List<Result>> get_scan_results() {
        return this.repository.get_scan_results();
    }

    /**
     * mock results by directly and asynchronously writing results into the database
     */
    public void mock_results() {
        this.repository.mock_results();
    }

    public void storeBBoxList(ArrayList<int[]> bBoxList) {
        this.repository.storeBBoxList(bBoxList);
    }

    /**
     * save file name and path of the xml file into the repository
     */

    public void store_xml(Uri uri_xml) {
        this.repository.store_xml(uri_xml);
    }

    public void export_xml(String str_box_list) {
        this.repository.export_xml(str_box_list);
    }

    public void store_list_and_pair(ArrayList<Uri> list_uri_jpg, ArrayList<Uri> list_uri_xml, ArrayList<int[]> pair) {
        this.repository.store_list_and_pair(list_uri_jpg, list_uri_xml, pair);
    }

    public ArrayList<int[]> get_pair() {
        return this.repository.get_pair_jpg_xml();
    }

    public ArrayList<Uri> get_list_jpg() {
        return this.repository.get_list_uri_jpg();
    }

    public ArrayList<Uri> get_list_xml() {
        return this.repository.get_list_uri_xml();
    }

    public void store_result_list(ArrayList<ScanResult> resultList) {
        this.repository.store_result_list(resultList);
    }

//    public void show_xml() {
//        this.repository.show_scan_results_xml();
//    }
}
