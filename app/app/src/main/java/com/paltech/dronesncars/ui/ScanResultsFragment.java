package com.paltech.dronesncars.ui;

import static android.app.Activity.RESULT_OK;

import static org.chromium.base.ThreadUtils.runOnUiThread;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.computing.PrePostProcessor;
import com.paltech.dronesncars.computing.ScanResult;
import com.paltech.dronesncars.computing.XMLParser;
import com.paltech.dronesncars.databinding.FragmentScanResultsBinding;
import com.paltech.dronesncars.model.Result;


import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A Fragment displaying the results of the Computer-Vision-Pipeline e.g. which unwanted
 * plants were found and where. (Computer-Vision-Pipeline currently completely mocked) A subclass
 * of {@link LandscapeFragment}.
 */
public class ScanResultsFragment extends LandscapeFragment<FragmentScanResultsBinding, ScanResultsViewModel> implements Runnable {

    private FragmentScanResultsBinding view_binding;

    /**
     * the RecyclerAdapter used to configure the Results-Recycler-View
     */
    private ScanResultRecyclerAdapter result_recycler_adapter;

    private ScanResultsViewModel view_model;

    private int current_jpg = -1;

    private Bitmap mBitmap = null;
    private Module mModule = null;
    private String mImageName = null;
    private int mImageWidth = 0;
    private int mImageHeight = 0;

    ArrayList<Uri> list_uri_jpg, list_uri_xml;
    ArrayList<int[]> list_index_pair;

    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;


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

        view_binding.btnAddWeedMarker.setEnabled(false);
        view_binding.btnRemoveWeedMarker.setEnabled(false);
        view_binding.btnResizeWeedMarker.setEnabled(false);
        setListeners();
        init_result_recycler_view();
//        set_livedata_sources();

        //load the yolo model
        try {
            mModule = LiteModuleLoader.load(assetFilePath(getContext(), "best.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getContext().getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
        }

    }

    /**
     * initial configuration of the Result-Recycler-View
     */
    private void init_result_recycler_view() {
        RecyclerView.LayoutManager layout_manager = new LinearLayoutManager(getActivity());
        view_binding.recyclerViewResults.setLayoutManager(layout_manager);
        view_binding.recyclerViewResults.scrollToPosition(0);

        result_recycler_adapter = new ScanResultRecyclerAdapter(new ArrayList<>(), requireContext());
        result_recycler_adapter.setOnItemClickListener(new ScanResultRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                //TODO
                int position = view_binding.recyclerViewResults.getChildAdapterPosition(view);
                if (view_binding.viewScanResults.highlight(position)) {
                    view_binding.btnResizeWeedMarker.setEnabled(false);
                    view_binding.btnAddWeedMarker.setEnabled(false);
                    view_binding.btnRemoveWeedMarker.setEnabled(false);
                } else {
                    view_binding.btnResizeWeedMarker.setEnabled(true);
                    view_binding.btnAddWeedMarker.setEnabled(true);
                    view_binding.btnRemoveWeedMarker.setEnabled(true);
                }
            }
        });
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
//    private void set_livedata_sources() {
//        view_model.get_scan_results().observe(getViewLifecycleOwner(),
//                results -> result_recycler_adapter.set_local_scan_results(results));
//    }

    /**
     * configure the listeners:
     * 1. buttonConfigureRovers -> change the view to the next fragment {@link RoverRouteFragment}
     * 2. buttonOpenGallery -> opens the Gallery where in theory pictures of the results could be
     * found
     * 3. buttonMockResults -> trigger the mocking of some results. Can/has to be removed as soon as
     * there is a real Computer-Vision-Pipeline
     * 4. buttonOpenGallery -> picks one image from gallery and show it on the imageview (replaced by buttonMulti)
     * 5. buttonMockResults -> transports images into YOLO model and get results xml
     * 6. buttonAddWeedMarker -> adds markers manually
     * 7. buttonResizeWeedMarker -> resizes markers manually
     * 8. buttonRemoveWeedMarker -> removes markers manually
     * 9. buttonOpenMultipleFiles -> picks multiple images and xml files to match each other and shows the 1st pair on the view
     * 10. buttonLastPic -> shows last pic (if available) and stores current results
     * 11. buttonNextPic -> shows next pic (if available) and stores current results
     */
    private void setListeners() {
        view_binding.buttonConfigureRovers.setOnClickListener(v -> {
            save();
            NavDirections action = ScanResultsFragmentDirections.actionScanResultsFragmentToRoverRouteFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });

        view_binding.buttonOpenGallery.setOnClickListener(v -> {
////            Intent open_gallery_intent = new Intent();
////            open_gallery_intent.setAction(Intent.ACTION_PICK); /*change from action_view to ACTION_PICK*/
//            Intent open_gallery_intent = new Intent(Intent.ACTION_GET_CONTENT);
////            Intent open_gallery_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            open_gallery_intent.setType("*/*");
////            open_gallery_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            startActivity(open_gallery_intent);

            Intent open_gallery_intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            open_gallery_intent.setType("*/*");
            open_gallery_intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            getPicFromGallery.launch(open_gallery_intent);
            show_toast("Please select from left drawer");
        });

//        view_binding.buttonMockResults.setOnClickListener(v -> mock_results());
        view_binding.buttonMockResults.setOnClickListener(v -> {
            Thread thread = new Thread(ScanResultsFragment.this);
            thread.start();
//            afterMock();
        });

        view_binding.btnAddWeedMarker.setOnClickListener(v -> {
            if (!view_binding.btnAddWeedMarker.isSelected()) {
                view_binding.viewScanResults.state = Edit_state.Add;
                view_binding.btnAddWeedMarker.setSelected(true);
                view_binding.btnAddWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_light_green));
            } else {
                view_binding.viewScanResults.state = Edit_state.None;
                view_binding.btnAddWeedMarker.setSelected(false);
                view_binding.btnAddWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            }
            view_binding.btnResizeWeedMarker.setSelected(false);
            view_binding.btnRemoveWeedMarker.setSelected(false);
            view_binding.btnResizeWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            view_binding.btnRemoveWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            save();
        });

        view_binding.btnResizeWeedMarker.setOnClickListener(v -> {
            if (!view_binding.btnResizeWeedMarker.isSelected()) {
                view_binding.viewScanResults.state = Edit_state.Resize;
                view_binding.btnResizeWeedMarker.setSelected(true);
                view_binding.btnResizeWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_light_green));
            } else {
                view_binding.viewScanResults.state = Edit_state.None;
                view_binding.btnResizeWeedMarker.setSelected(false);
                view_binding.btnResizeWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            }
            view_binding.btnAddWeedMarker.setSelected(false);
            view_binding.btnRemoveWeedMarker.setSelected(false);
            view_binding.btnAddWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            view_binding.btnRemoveWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            save();
        });

        view_binding.btnRemoveWeedMarker.setOnClickListener(v -> {
            if (!view_binding.btnRemoveWeedMarker.isSelected()) {
                view_binding.viewScanResults.state = Edit_state.Remove;
                view_binding.btnRemoveWeedMarker.setSelected(true);
                view_binding.btnRemoveWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_light_green));
            } else {
                view_binding.viewScanResults.state = Edit_state.None;
                view_binding.btnRemoveWeedMarker.setSelected(false);
                view_binding.btnRemoveWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            }
            view_binding.btnAddWeedMarker.setSelected(false);
            view_binding.btnResizeWeedMarker.setSelected(false);
            view_binding.btnAddWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            view_binding.btnResizeWeedMarker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.paltech_green));
            save();
        });

        view_binding.buttonOpenMultipleFiles.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            selectMultiFiles.launch(intent);
        });

        view_binding.btnLastPic.setOnClickListener(v -> {
            save();
            current_jpg--;
            fresh_iv_res(current_jpg);
        });

        view_binding.btnNextPic.setOnClickListener(v -> {
            save();
            current_jpg++;
            fresh_iv_res(current_jpg);
        });
    }

    ActivityResultLauncher<Intent> selectMultiFiles = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            int currentItem = 0;
                            ArrayList<Uri> list_uri_jpg = new ArrayList<>();
                            ArrayList<Uri> list_uri_xml = new ArrayList<>();
                            ArrayList<int[]> list_index_pair = new ArrayList<>();
                            int jpg_count, xml_count, pair_count;
                            while (currentItem < count) {
                                Uri fileUri = data.getClipData().getItemAt(currentItem).getUri();
                                String type = fileUri.getPath().substring(fileUri.getPath().lastIndexOf(".") + 1);
                                if (type.equals("jpg")) {
                                    list_uri_jpg.add(fileUri);
                                } else if (type.equals("xml")) {
                                    list_uri_xml.add(fileUri);
                                }
                                currentItem = currentItem + 1;
                            }
                            jpg_count = list_uri_jpg.size();
                            xml_count = list_uri_xml.size();
                            for (int i = 0; i < jpg_count; i++) {
                                Uri jpgUri = list_uri_jpg.get(i);
                                String jpgPath = jpgUri.getPath();
                                String jpgFileName = jpgPath.substring(jpgPath.lastIndexOf("/"), jpgPath.lastIndexOf("."));
                                int j = 0;
                                while (j < xml_count) {
                                    Uri xmlUri = list_uri_xml.get(j);
                                    String xmlPath = xmlUri.getPath();
                                    String xmlFileName = xmlPath.substring(xmlPath.lastIndexOf("/"), xmlPath.lastIndexOf("."));
                                    if (jpgFileName.equals(xmlFileName)) {
                                        break;
                                    } else {
                                        j++;
                                    }
                                }
                                if (j < xml_count) {
                                    list_index_pair.add(new int[]{i, j});
                                }
                            }
                            pair_count = list_index_pair.size();
                            Toast.makeText(getContext(), "" + jpg_count + " images imported and "
                                    + xml_count + " XML files imported in which " + pair_count +
                                    " pairs detected!", Toast.LENGTH_LONG).show();
                            if (pair_count > 0) {
                                current_jpg = 0;
                                Uri uri_jpg = list_uri_jpg.get(list_index_pair.get(0)[0]);
                                Uri uri_xml = list_uri_xml.get(list_index_pair.get(0)[1]);
                                Bitmap bitmap = null;
                                try {
                                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri_jpg);
                                    bitmap = BitmapFactory.decodeStream(inputStream);
                                    mBitmap = bitmap;
                                    view_binding.imageViewStitchedImage.setImageBitmap(bitmap);
                                    Log.e("ivwidth", String.valueOf(view_binding.imageViewStitchedImage.getWidth()));
                                    Log.e("rvwidth", String.valueOf(view_binding.viewScanResults.getWidth()));
                                    XMLParser.setResultViewWidth(view_binding.viewScanResults.getWidth());
                                    XMLParser.setResultViewHeight(view_binding.viewScanResults.getHeight());
                                    ArrayList<ScanResult> resultList = XMLParser.parseXMLFile(uri_xml, getContext());
                                    store_result_list(resultList);
                                    store_xml_uri(uri_xml);
                                    ScanResultsView scanResultsView = view_binding.viewScanResults;
                                    scanResultsView.setScanResults(resultList);
                                    scanResultsView.postInvalidate();
                                    store_list_and_pair(list_uri_jpg, list_uri_xml, list_index_pair);
                                    view_binding.btnAddWeedMarker.setEnabled(true);
                                    view_binding.btnRemoveWeedMarker.setEnabled(true);
                                    view_binding.btnResizeWeedMarker.setEnabled(true);
                                    fresh_iv_res(0);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (data.getData() != null) {
                            String imagePath = data.getData().getPath();
                            //do something with the image (save it to some directory or whatever you need to do with it here)
                        }
                    }
                }
            }
    );


    /**
     *
     */

    ActivityResultLauncher<Intent> launcherImportXml = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri uri_xml = data.getData();
                        ArrayList<ScanResult> resultList = XMLParser.parseXMLFile(uri_xml, getContext());
                        store_result_list(resultList);
                        store_xml_uri(uri_xml);

                        ScanResultsView scanResultsView = view_binding.viewScanResults;
                        scanResultsView.setScanResults(resultList);
                        scanResultsView.postInvalidate();
                    }
                }
            }
    );


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
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            int currentItem = 0;
                            int jpg_count;
                            list_uri_jpg = new ArrayList<>();
                            list_uri_xml = new ArrayList<>();
                            list_index_pair = new ArrayList<>();
                            while (currentItem < count) {
                                Uri fileUri = data.getClipData().getItemAt(currentItem).getUri();
                                String type = fileUri.getPath().substring(fileUri.getPath().lastIndexOf(".") + 1);
                                if (type.equals("jpg")) {
                                    list_uri_jpg.add(fileUri);
                                }
                                currentItem = currentItem + 1;
                            }
                            jpg_count = list_uri_jpg.size();
                            Toast.makeText(getContext(), "" + jpg_count + " images imported!", Toast.LENGTH_LONG).show();
                            if (jpg_count > 0) {
                                current_jpg = 0;
                                Uri uri_jpg = list_uri_jpg.get(0);
                                String uri_Path = uri_jpg.getPath();
                                String uri_jpg_Path = uri_Path.substring(uri_Path.indexOf(":") + 1);
                                mImageName = uri_jpg_Path.substring(uri_jpg_Path.lastIndexOf("/") + 1, uri_jpg_Path.lastIndexOf("."));
                                Bitmap bitmap = null;
                                try {
                                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri_jpg);
                                    bitmap = BitmapFactory.decodeStream(inputStream);
                                    mBitmap = bitmap;
                                    view_binding.imageViewStitchedImage.setImageBitmap(bitmap);
                                    XMLParser.setResultViewWidth(view_binding.viewScanResults.getWidth());
                                    XMLParser.setResultViewHeight(view_binding.viewScanResults.getHeight());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
//                                view_binding.btnAddWeedMarker.setEnabled(true);
//                                view_binding.btnRemoveWeedMarker.setEnabled(true);
//                                view_binding.btnResizeWeedMarker.setEnabled(true);
                            }
                        } else if (data.getData() != null) {
                            String imagePath = data.getData().getPath();
                            //do something with the image (save it to some directory or whatever you need to do with it here)
                        }








//                        Uri uri_picked_image = data.getData();
//                        String uri_Path = uri_picked_image.getPath();
//                        String uri_jpg_Path = uri_Path.substring(uri_Path.indexOf(":") + 1);
////                        String uri_xml_path = uri_jpg_Path.substring(0, uri_jpg_Path.lastIndexOf(".")) + ".xml";
//                        mImageName = uri_jpg_Path.substring(uri_jpg_Path.lastIndexOf("/") + 1, uri_jpg_Path.lastIndexOf("."));
////                        String xmlPath = uri_xml_path.substring(0, uri_xml_path.lastIndexOf("/"));
////                        // VID_20201007_134751.mp4_frame33.jpg
////                        String xmlName = uri_xml_path.substring(uri_xml_path.lastIndexOf("/") + 1);
//////                        String xmlPath3 ="Download";
//////                        String xmlPath4 = "VID_20201007_134751.mp4_frame33.xml";
////                        //content://com.android.externalstorage.documents/document/
////                        // primary%3ADownload%2FVID_20201007_134751.mp4_frame33.jpg
////                        Log.e("uri", "" + uri_picked_image.toString());
////                        ///document/primary:Download/VID_20201007_134751.mp4_frame33.jpg
////                        Log.e("uripath", "" + uri_Path);
////                        Log.e("uri_xml_path", "" + uri_xml_path);
//////                        Log.e("xmlpath",""+xmlPath);
////////                        Log.e("xml",""+xmlStr);
//////                        FileInputStream fileInputStream = null;
////                        File xmlFile1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.pathSeparator + xmlPath, xmlName);
////                        File xmlFile2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), uri_xml_path);
////////                        File xmlFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+xmlPath3, xmlPath4);
////                        Log.e("xmlfile1", "" + xmlFile1.exists());
////                        Log.e("xmlfile2", "" + xmlFile2.exists());
//
//
////                        Uri uri_xml = Uri.parse(uri_xml_path);
//                        Bitmap bitmap = null;
//                        try {
//                            InputStream inputStream = getContext().getContentResolver().openInputStream(uri_picked_image);
//                            bitmap = BitmapFactory.decodeStream(inputStream);
//                            mImageWidth = bitmap.getWidth();
//                            mImageHeight = bitmap.getHeight();
//                            mBitmap = bitmap;
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
////                        ArrayList<double[]> bListMock = XMLParser.parseXMLFile(uri_xml, getContext());
////                        ArrayList<double[]> bListMock = XMLParser.parseXMLFile(Uri.parse(uri_com), getContext());
////                        ScanResultsView scanResultsView = (ScanResultsView) view_binding.viewScanResults;
//
////                        scanResultsView.setBgAndBBL(bitmap, bListMock);
////                        scanResultsView.postInvalidate();
////                        scanResultsView.setScanResultsFragment(this);
//                        view_binding.imageViewStitchedImage.setImageBitmap(bitmap);
//                        view_binding.btnResizeWeedMarker.setEnabled(true);
//                        view_binding.btnRemoveWeedMarker.setEnabled(true);
//                        view_binding.btnAddWeedMarker.setEnabled(true);
//
////                        view_binding.imageViewStitchedImage.setImageURI(uri);
////                        view_binding.imageViewMockedResults.setImageURI(uri);
////                        view_model.store_xml(xmlPath, xmlName);
////                        view_model.show_xml();
                    }
                }
            }
    );

    private void store_BBoxList(ArrayList<int[]> bBoxList) {
        view_model.storeBBoxList(bBoxList);
    }

    private void store_xml_uri(Uri uri_xml) {
        view_model.store_xml(uri_xml);
    }

    private void export_xml() {
        String str_box_list = view_binding.viewScanResults.export();
        view_model.export_xml(str_box_list);
    }

    /**
     * stores the lists of images and xml files and the pairing into repo
     *
     * @param list_uri_jpg
     * @param list_uri_xml
     * @param pair
     */
    private void store_list_and_pair(ArrayList<Uri> list_uri_jpg, ArrayList<Uri> list_uri_xml, ArrayList<int[]> pair) {
        view_model.store_list_and_pair(list_uri_jpg, list_uri_xml, pair);

    }

    private void fresh_iv_res(int current_jpg) {
        ArrayList<int[]> list_index_pair = view_model.get_pair();
        ArrayList<Uri> list_uri_jpg = view_model.get_list_jpg();
        ArrayList<Uri> list_uri_xml = view_model.get_list_xml();
        if (current_jpg == 0) {
            view_binding.btnLastPic.setEnabled(false);
        } else {
            view_binding.btnLastPic.setEnabled(true);
        }

        if (current_jpg == list_index_pair.size() - 1) {
            view_binding.btnNextPic.setEnabled(false);
        } else {
            view_binding.btnNextPic.setEnabled(true);
        }
        view_binding.btnAddWeedMarker.setEnabled(true);
        view_binding.btnRemoveWeedMarker.setEnabled(true);
        view_binding.btnResizeWeedMarker.setEnabled(true);

        Uri uri_jpg = list_uri_jpg.get(list_index_pair.get(current_jpg)[0]);
        Uri uri_xml = list_uri_xml.get(list_index_pair.get(current_jpg)[1]);
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri_jpg);
            bitmap = BitmapFactory.decodeStream(inputStream);
            mBitmap = bitmap;
            view_binding.imageViewStitchedImage.setImageBitmap(bitmap);
            XMLParser.setResultViewWidth(view_binding.viewScanResults.getWidth());
            XMLParser.setResultViewHeight(view_binding.viewScanResults.getHeight());
            ArrayList<ScanResult> resultList = XMLParser.parseXMLFile(uri_xml, getContext());
            store_result_list(resultList);
            store_xml_uri(uri_xml);
            ScanResultsView scanResultsView = view_binding.viewScanResults;
            scanResultsView.setScanResults(resultList);
            scanResultsView.postInvalidate();
            ScanResultRecyclerAdapter adapter = (ScanResultRecyclerAdapter) view_binding.recyclerViewResults.getAdapter();
            adapter.set_local_scan_results(resultList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void store_result_list(ArrayList<ScanResult> resultList) {
        view_model.store_result_list(resultList);
    }

    private void save() {
        ArrayList<int[]> list_index_pair = view_model.get_pair();
        ArrayList<Uri> list_uri_xml = view_model.get_list_xml();
        view_binding.viewScanResults.setScale_height_result_image((float) 1 / XMLParser.getScale_height_image_result());
        view_binding.viewScanResults.setScale_width_result_image((float) 1 / XMLParser.getScale_width_image_result());
        String str_box_list = view_binding.viewScanResults.export();
        view_model.export_xml(str_box_list);

    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    public void run() {

        for (int image = 0; image < list_uri_jpg.size(); image++) {

            Uri uri_jpg = list_uri_jpg.get(image);
            String uri_Path = uri_jpg.getPath();
            String uri_jpg_Path = uri_Path.substring(uri_Path.indexOf(":") + 1);
            mImageName = uri_jpg_Path.substring(uri_jpg_Path.lastIndexOf("/") + 1, uri_jpg_Path.lastIndexOf("."));
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(uri_jpg);
                mBitmap = BitmapFactory.decodeStream(inputStream);
                mImageHeight = mBitmap.getHeight();
                mImageWidth = mBitmap.getWidth();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //1920 --> 640; 1080 -->640
            mImgScaleX = (float) mBitmap.getWidth() / PrePostProcessor.mInputWidth;
            mImgScaleY = (float) mBitmap.getHeight() / PrePostProcessor.mInputHeight;

            //932 --> 1920
            mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float) view_binding.imageViewStitchedImage.getWidth() / mBitmap.getWidth() : (float) view_binding.imageViewStitchedImage.getHeight() / mBitmap.getHeight());
            mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth() ? (float) view_binding.imageViewStitchedImage.getHeight() / mBitmap.getHeight() : (float) view_binding.imageViewStitchedImage.getWidth() / mBitmap.getWidth());

            mStartX = (view_binding.imageViewStitchedImage.getWidth() - mIvScaleX * mBitmap.getWidth()) / 2;
            mStartY = (view_binding.imageViewStitchedImage.getHeight() - mIvScaleY * mBitmap.getHeight()) / 2;
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
            final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
            IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
            final Tensor outputTensor = outputTuple[0].toTensor();
            final float[] outputs = outputTensor.getDataAsFloatArray();
            final ArrayList<ScanResult> results = PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

            String out = "<annotation verified=\"yes\">" +
                    "<folder>Annotation</folder>" +
                    "<filename>" + mImageName + ".jpg" + "</filename>" +
                    "<path>../../workspace/training_demo/images/ready_for_review\\" + mImageName + ".jpg" + "</path>" +
                    "<source><database>Unknown</database></source>" +
                    "<size><width>" + mImageWidth + "</width><height>" + mImageHeight + "</height><depth>3</depth></size><segmented>0</segmented>";
            if (results.size() > 0) {
                for (int i = 0; i < results.size(); i++) {
                    out = out + "<object>" +
                            "<name>Sorrel</name>" +
                            "<pose>Unspecified</pose>" +
                            "<truncated>0</truncated>" +
                            "<difficult>0</difficult>";
                    out = out + "<className>" + results.get(i).getClassName() + "</className>";
                    out = out + "<confidence>" + results.get(i).getScore() + "</confidence>";
                    out = out + "<bndbox>";
                    Rect rect = results.get(i).getRect();
                    out = out + "<xmin>" + rect.left + "</xmin>";
                    out = out + "<ymin>" + rect.top + "</ymin>";
                    out = out + "<xmax>" + rect.right + "</xmax>";
                    out = out + "<ymax>" + rect.bottom + "</ymax>";
                    out = out + "</bndbox>" + "</object>";
                }

            }
            out = out + "</annotation>";
            Log.e("out", out);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, mImageName + ".xml");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "xml/xml");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Paltech");
            }
            Uri uri_write = null;
            ContentResolver contentResolver = getContext().getContentResolver();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                uri_write = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            }
            if (uri_write != null) {
                try {
                    OutputStream outputStream = contentResolver.openOutputStream(uri_write, "wt");//TODO rwt
                    outputStream.write(out.getBytes(StandardCharsets.UTF_8));
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            list_uri_xml.add(uri_write);
            list_index_pair.add(new int[]{image, image});
//            XMLParser.setResultViewWidth(view_binding.viewScanResults.getWidth());
//            XMLParser.setResultViewHeight(view_binding.viewScanResults.getHeight());
//            ArrayList<ScanResult> scanResults = XMLParser.parseXMLFile(uri_write, this.getContext());
//            view_binding.viewScanResults.setScanResults(scanResults);
//            view_binding.viewScanResults.postInvalidate();
//            ScanResultRecyclerAdapter adapter = (ScanResultRecyclerAdapter) view_binding.recyclerViewResults.getAdapter();
//            runOnUiThread(() -> {
//                adapter.set_local_scan_results(scanResults);
//                view_binding.btnAddWeedMarker.setEnabled(true);
//                view_binding.btnRemoveWeedMarker.setEnabled(true);
//                view_binding.btnResizeWeedMarker.setEnabled(true);
//                fresh_iv_res(0);
//                });
        }
        runOnUiThread(() -> {
            store_list_and_pair(list_uri_jpg, list_uri_xml, list_index_pair);
            fresh_iv_res(0);
        });
//        Toast.makeText(getContext(), "finished", Toast.LENGTH_LONG).show();
    }

    private void afterMock() {
        Uri uri_xml = list_uri_xml.get(0);
        ArrayList<ScanResult> resultList = XMLParser.parseXMLFile(uri_xml, getContext());
        store_result_list(resultList);
        store_xml_uri(uri_xml);
        ScanResultsView scanResultsView = view_binding.viewScanResults;
        scanResultsView.setScanResults(resultList);
        scanResultsView.postInvalidate();
        store_list_and_pair(list_uri_jpg, list_uri_xml, list_index_pair);
        view_binding.btnAddWeedMarker.setEnabled(true);
        view_binding.btnRemoveWeedMarker.setEnabled(true);
        view_binding.btnResizeWeedMarker.setEnabled(true);
        fresh_iv_res(0);
    }

}