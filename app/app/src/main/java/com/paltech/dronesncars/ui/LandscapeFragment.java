package com.paltech.dronesncars.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

public abstract class LandscapeFragment<View_Binding extends ViewBinding, View_Model extends ViewModel> extends Fragment {

    protected View_Binding view_binding;
    protected View_Model view_model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    abstract View_Binding get_view_binding(View view);

    abstract View_Model get_view_model();

    public void show_toast(String msg){
        requireActivity().runOnUiThread(()->{
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }
}
