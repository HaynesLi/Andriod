package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Result;

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

    /**
     * save file name and path of the xml file into the repository
     */

    public void store_xml(String path, String file) {
        this.repository.store_xml_file(path, file);
    }

//    public void show_xml() {
//        this.repository.show_scan_results_xml();
//    }
}
