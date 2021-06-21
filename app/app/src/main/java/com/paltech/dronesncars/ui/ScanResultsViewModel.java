package com.paltech.dronesncars.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.paltech.dronesncars.model.Repository;
import com.paltech.dronesncars.model.Result;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScanResultsViewModel extends ViewModel {

    private final Repository repository;

    @Inject
    public ScanResultsViewModel(Repository repository) {
        this.repository = repository;
    }

    public LiveData<List<Result>> get_scan_results() {
        return this.repository.get_scan_results();
    }

    public void mock_results() {
        this.repository.mock_results();
    }

}
