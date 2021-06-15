package com.paltech.dronesncars.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentScanResultsBinding;
import com.paltech.dronesncars.model.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanResultsFragment extends LandscapeFragment {

    private FragmentScanResultsBinding view_binding;
    private ScanResultRecyclerAdapter result_recycler_adapter;
    private ScanResultsViewModel view_model;

    public ScanResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanResultsFragment.
     */
    public static ScanResultsFragment newInstance(String param1, String param2) {
        ScanResultsFragment fragment = new ScanResultsFragment();
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
        return inflater.inflate(R.layout.fragment_scan_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = FragmentScanResultsBinding.bind(view);
        view_model = new ViewModelProvider(requireActivity()).get(ScanResultsViewModel.class);

        setListeners();
        init_result_recycler_view();
        set_livedata_sources();
        mock_results();
    }

    private void init_result_recycler_view() {
        RecyclerView.LayoutManager layout_manager = new LinearLayoutManager(getActivity());
        view_binding.recyclerViewResults.setLayoutManager(layout_manager);
        view_binding.recyclerViewResults.scrollToPosition(0);

        result_recycler_adapter = new ScanResultRecyclerAdapter(new ArrayList<>(), requireContext());
        view_binding.recyclerViewResults.setAdapter(result_recycler_adapter);
    }

    private void mock_results() {
        view_model.mock_scan_results();
    }

    private void set_livedata_sources() {
        view_model.get_scan_results().observe(getViewLifecycleOwner(),
                results -> result_recycler_adapter.set_local_scan_results(results));
    }

    private void setListeners() {
        view_binding.buttonConfigureRovers.setOnClickListener(v -> {
            NavDirections action = ScanResultsFragmentDirections.actionScanResultsFragmentToRoverRouteFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
    }

}