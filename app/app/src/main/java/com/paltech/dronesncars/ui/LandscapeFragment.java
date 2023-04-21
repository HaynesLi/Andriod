package com.paltech.dronesncars.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.viewbinding.ViewBinding;

/**
 * A Fragment which serves as the general supertype of each all other fragments and, most
 * importantly, sets the requested orientation of our fragments on the screen to landscape.
 * It is a subclass of {@link Fragment}.
 * @param <View_Binding> the ViewBinding to use, has to be a subclass of {@link ViewBinding}
 * @param <View_Model> the ViewModel to use, has to be a subclass of {@link ViewModel}
 */
public abstract class LandscapeFragment<View_Binding extends ViewBinding, View_Model extends ViewModel> extends Fragment {

    /**
     * the fragments ViewBinding
     */
    protected View_Binding view_binding;
    /**
     * the fragments ViewModel
     */
    protected View_Model view_model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * Get the specified ViewBinding
     * Change the content of this method to change the used ViewBinding
     * @param view the view to get the ViewBinding for
     * @return the ViewBinding
     */
    abstract View_Binding get_view_binding(View view);

    /**
     * Get the specified ViewModel
     * Change the content of this method to change the used ViewModel
     * @return the ViewModel
     */
    abstract View_Model get_view_model();

    /**
     * Show a String message on the screen and do it from the UI-Thread, in case
     * the message originates from a computing thread.
     * @param msg the message to display
     */
    public void show_toast(String msg){
        requireActivity().runOnUiThread(()-> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
    }
}
