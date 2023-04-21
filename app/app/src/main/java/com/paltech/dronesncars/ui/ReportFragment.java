package com.paltech.dronesncars.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.FragmentReportBinding;

/**
 * A Fragment which Is supposed to show interesting data after finishing the weed picking, but does
 * not show anything yet. It is a subclass of {@link LandscapeFragment}.
 */
public class ReportFragment extends LandscapeFragment<FragmentReportBinding, ViewModel> {

    private FragmentReportBinding view_binding;

    public ReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReportFragment.
     */
    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    FragmentReportBinding get_view_binding(View view) {
        return FragmentReportBinding.bind(view);
    }

    @Override
    ViewModel get_view_model() {
        return null;
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    /**
     * one of a fragments basic lifecycle methods {@link androidx.fragment.app.Fragment#onViewCreated(View, Bundle)}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}