package com.paltech.dronesncars.ui;

import android.content.Intent;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanResultsFragment extends LandscapeFragment<FragmentScanResultsBinding, ScanResultsViewModel> {

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
    FragmentScanResultsBinding get_view_binding(View view) {
        return FragmentScanResultsBinding.bind(view);
    }

    @Override
    ScanResultsViewModel get_view_model() {
        return new ViewModelProvider(requireActivity()).get(ScanResultsViewModel.class);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan_results, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     * 1. gets ViewBinding
     * 2. gets ViewModel
     * 3. triggers configuration of Listeners
     * 4. triggers configuration of scan-result-recycler-view
     * 5. triggers configuration of LiveData-Sources
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view_binding = get_view_binding(view);
        view_model = get_view_model();

        setListeners();
        init_result_recycler_view();
        set_livedata_sources();
    }

    private void init_result_recycler_view() {
        RecyclerView.LayoutManager layout_manager = new LinearLayoutManager(getActivity());
        view_binding.recyclerViewResults.setLayoutManager(layout_manager);
        view_binding.recyclerViewResults.scrollToPosition(0);

        result_recycler_adapter = new ScanResultRecyclerAdapter(new ArrayList<>(), requireContext());
        view_binding.recyclerViewResults.setAdapter(result_recycler_adapter);
    }

    private void mock_results() {
        view_model.mock_results();
    }

    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     */
    private void set_livedata_sources() {
        view_model.get_scan_results().observe(getViewLifecycleOwner(),
                results -> result_recycler_adapter.set_local_scan_results(results));
    }

    private void setListeners() {
        view_binding.buttonConfigureRovers.setOnClickListener(v -> {
            NavDirections action = ScanResultsFragmentDirections.actionScanResultsFragmentToRoverRouteFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
        view_binding.buttonOpenGallery.setOnClickListener(v -> {
            Intent open_gallery_intent = new Intent();
            open_gallery_intent.setAction(Intent.ACTION_VIEW);
            open_gallery_intent.setType("image/*");
            open_gallery_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(open_gallery_intent);
        });
        view_binding.buttonMockResults.setOnClickListener(v -> mock_results());
    }

}