package com.paltech.dronesncars.ui;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.computing.XMLParser;
import com.paltech.dronesncars.databinding.FragmentScanResultsBinding;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A Fragment displaying the results of the Computer-Vision-Pipeline e.g. which unwanted
 * plants were found and where. (Computer-Vision-Pipeline currently completely mocked) A subclass
 * of {@link LandscapeFragment}.
 */
public class ScanResultsFragment extends LandscapeFragment<FragmentScanResultsBinding, ScanResultsViewModel> {

    private FragmentScanResultsBinding view_binding;

    /**
     * the RecyclerAdapter used to configure the Results-Recycler-View
     */
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
    public static ScanResultsFragment newInstance() {
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

    /**
     * initial configuration of the Result-Recycler-View
     */
    private void init_result_recycler_view() {
        RecyclerView.LayoutManager layout_manager = new LinearLayoutManager(getActivity());
        view_binding.recyclerViewResults.setLayoutManager(layout_manager);
        view_binding.recyclerViewResults.scrollToPosition(0);

        result_recycler_adapter = new ScanResultRecyclerAdapter(new ArrayList<>(), requireContext());
        view_binding.recyclerViewResults.setAdapter(result_recycler_adapter);
    }

    /**
     * trigger the view model to mock some results (has to be replaced/removed as soon as there is a
     * real Computer-Vision-Pipeline)
     */
    private void mock_results() {
        view_model.mock_results();
    }

    /**
     * Configures the Fragment as Observer for different LiveData-Sources of the ViewModel and
     * specifies callbacks, which are called when the observed LiveData-Source is changed.
     * 1. get_scan_results() -> update the results displayed in the Results-Recycler-View
     */
    private void set_livedata_sources() {
        view_model.get_scan_results().observe(getViewLifecycleOwner(),
                results -> result_recycler_adapter.set_local_scan_results(results));
    }

    /**
     * configure the listeners:
     * 1. buttonConfigureRovers -> change the view to the next fragment {@link RoverRouteFragment}
     * 2. buttonOpenGallery -> opens the Gallery where in theory pictures of the results could be
     * found
     * 3. buttonMockResults -> trigger the mocking of some results. Can/has to be removed as soon as
     * there is a real Computer-Vision-Pipeline
     */
    private void setListeners() {
        view_binding.buttonConfigureRovers.setOnClickListener(v -> {
            NavDirections action = ScanResultsFragmentDirections.actionScanResultsFragmentToRoverRouteFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
        view_binding.buttonOpenGallery.setOnClickListener(v -> {
//            Intent open_gallery_intent = new Intent();
//            open_gallery_intent.setAction(Intent.ACTION_PICK); /*change from action_view to ACTION_PICK*/
            Intent open_gallery_intent = new Intent(Intent.ACTION_GET_CONTENT);
            open_gallery_intent.setType("*/*");
//            open_gallery_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(open_gallery_intent);
            getPicFromGallery.launch(open_gallery_intent);
            show_toast("Please select from left drawer");
        });
        view_binding.buttonMockResults.setOnClickListener(v -> mock_results());
    }

    /**
     * select one picture from gallery and return as a intent to imageview
     * better way is to move this block into scanresultsviewmodel or repository
     */
    ActivityResultLauncher<Intent> getPicFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        String uri_Path = uri.getPath();
                        String uri_jpg_Path = uri_Path.substring(uri_Path.indexOf(":") + 1);
                        String uri_xml_path = uri_jpg_Path.substring(0, uri_jpg_Path.lastIndexOf(".")) + ".xml";
                        uri_xml_path = uri_xml_path.trim();
                        String xmlPath = uri_xml_path.substring(0, uri_xml_path.lastIndexOf("/"));
                        String xmlName = uri_xml_path.substring(xmlPath.lastIndexOf("/") + 1);
//                        String xmlPath3 ="Download";
//                        String xmlPath4 = "VID_20201007_134751.mp4_frame33.xml";
                        Log.e("uripath",""+uri.getPath());
                        Log.e("uri_xml_path",""+uri_xml_path);
//                        Log.e("xmlpath",""+xmlPath);
////                        Log.e("xml",""+xmlStr);
//                        FileInputStream fileInputStream = null;
                        File xmlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),xmlPath);
////                        File xmlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+xmlPath3, xmlPath4);
                        Log.e("xmlfile",""+xmlFile.exists());
//                        byte[] buff = new byte[1024];
//                        try {
//                            fileInputStream = new FileInputStream(xmlFile);
//                            StringBuilder sb = new StringBuilder("");
//                            int len = 0;
//                            while ((len = fileInputStream.read(buff)) > 0) {
//                                sb.append(new String(buff,0,len));
//                            }
//                            Log.e("xml",""+sb.toString());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        } finally {
//                            if (fileInputStream != null) {
//                                try {
//                                    fileInputStream.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                        }
//

                        Bitmap bitmap = null;
                        try {
                            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                            bitmap = BitmapFactory.decodeStream(inputStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
//                        ArrayList<double[]> bListMock = XMLParser.parseXMLFile(uri_xml_path,getContext());
                        ScanResultsView scanResultsView = (ScanResultsView) view_binding.viewScanResults;
                        ArrayList<double[]> blist = new ArrayList<>();
                        double[] box1 = new double[4];
                        box1[0] = 0.3;
                        box1[1] = 0.17;
                        box1[2] = 0.45;
                        box1[3] = 0.32;
                        double[] box2 = new double[4];
                        box2[0] = 0.89;
                        box2[1] = 0.26;
                        box2[2] = 0.99;
                        box2[3] = 0.39;
                        double[] box3 = new double[4];
                        box3[0] = 0.42;
                        box3[1] = 0.92;
                        box3[2] = 0.49;
                        box3[3] = 0.999;
                        double[] box4 = new double[4];
                        box4[0] = 0.145;
                        box4[1] = 0.792;
                        box4[2] = 0.271;
                        box4[3] = 0.999;
                        double[] box5 = new double[4];
                        box5[0] = 0.518;
                        box5[1] = 0.0;
                        box5[2] = 0.548;
                        box5[3] = 0.0917;
                        blist.add(box1);
                        blist.add(box2);
                        blist.add(box3);
                        blist.add(box4);
                        blist.add(box5);
                        scanResultsView.setBgAndBBL(bitmap,blist);
                        scanResultsView.postInvalidate();
                        view_binding.imageViewStitchedImage.setImageBitmap(bitmap);

//                        view_binding.imageViewStitchedImage.setImageURI(uri);
//                        view_binding.imageViewMockedResults.setImageURI(uri);
                        view_model.store_xml(xmlPath, xmlName);
//                        view_model.show_xml();



                    }
                }
            }
    );


}