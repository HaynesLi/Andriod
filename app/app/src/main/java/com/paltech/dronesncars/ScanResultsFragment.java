package com.paltech.dronesncars;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.paltech.dronesncars.databinding.FragmentScanResultsBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanResultsFragment extends Fragment {

    private FragmentScanResultsBinding view_binding;

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

        setListeners();
    }

    private void setListeners() {
        view_binding.buttonConfigureRovers.setOnClickListener(v -> {
            NavDirections action = ScanResultsFragmentDirections.actionScanResultsFragmentToRoverRouteFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
    }

}