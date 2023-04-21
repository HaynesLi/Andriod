package com.paltech.dronesncars.ui;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.computing.ScanResult;
import com.paltech.dronesncars.databinding.ScanResultsRowItemBinding;
import com.paltech.dronesncars.model.Result;

import java.util.List;

/**
 * The RecyclerAdapter used as interface for to the ScanResults-RecyclerView. A subclass of
 * {@link RecyclerView.Adapter}. This RecyclerView displays the Results found by the (currently
 * mocked) Computer-Vision-Pipeline for weed-detection.
 */
public class ScanResultRecyclerAdapter extends RecyclerView.Adapter<ScanResultRecyclerAdapter.ScanResultViewHolder>{

    /**
     * the result set currently displayed in the RecyclerView
     */
    private List<ScanResult> local_scan_results;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    /**
     * Constructor for ScanResultRecyclerAdapter
     * @param scan_results the list of results to fill the RecyclerView with initially
     * @param context the context used for assigning certain TextView using a Template
     */
    public ScanResultRecyclerAdapter(List<ScanResult> scan_results, Context context) {
        local_scan_results = scan_results;
        this.context = context;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ScanResultViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_results_row_item, parent, false);
        ScanResultViewHolder scanResultViewHolder = new ScanResultRecyclerAdapter.ScanResultViewHolder(view);
//        return new ScanResultRecyclerAdapter.ScanResultViewHolder(view);

        if (mOnItemClickListener != null) {
            scanResultViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(view);
                    if (!scanResultViewHolder.selected) {
                        scanResultViewHolder.selected = true;
                        scanResultViewHolder.get_frame_layout().setBackground(context.getDrawable(R.drawable.rectangle_red));
                    } else {
                        scanResultViewHolder.selected = false;
                        scanResultViewHolder.get_frame_layout().setBackground(context.getDrawable(R.drawable.rectangle));
                    }
                }
            });
        }
        return scanResultViewHolder;

    }

    /**
     * one of androids standard method called everytime the list changes and a result has to be bound
     * to a View item
     * 1. configures the different TextViews
     * @param holder the view item to bind the rover to
     * @param position the index of the holder, which equals the index of the rover in the
     * {@link #local_scan_results}
     */
    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ScanResultRecyclerAdapter.ScanResultViewHolder holder, int position) {
        ScanResult result = local_scan_results.get(position);
        holder.get_scan_result_id().setText(Integer.toString(position + 1));
        String certainty_string = context.getString(R.string.content_result_certainty, (int)(result.getScore() * 100 + 0.5));
        holder.get_scan_result_certainty().setText(certainty_string);
        holder.get_scan_result_class().setText(result.getClassName());



    }

    /**
     * get the number of items in the RecyclerView
     * @return number of items in the RecyclerView
     */
    @Override
    public int getItemCount() {
        return local_scan_results.size();
    }

    /**
     * set the {@link #local_scan_results} to a new list
     * if you don't use this function to change the set don't forget to use notifyDataSetChanged()!
     * @param scan_results the List to set the {@link #local_scan_results} to
     */
    public void set_local_scan_results(List<ScanResult> scan_results) {
        this.local_scan_results = scan_results;
        notifyDataSetChanged();
    }

    /**
     * The ViewHolder for one item in the RecyclerView, which represents one result.
     * A subclass of {@link RecyclerView.ViewHolder}
     */
    public static class ScanResultViewHolder extends RecyclerView.ViewHolder {

        ScanResultsRowItemBinding view_binding;
        boolean selected;

        public ScanResultViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            view_binding = ScanResultsRowItemBinding.bind(itemView);
            selected = false;
        }

        /**
         * get the TextView for the result id
         * @return TextView for the result id
         */
        public TextView get_scan_result_id() {
            return view_binding.contentScanResultId;
        }

        /**
         * get the TextView for the result certainty
         * @return TextView for the result certainty
         */
        public TextView get_scan_result_certainty() {
            return view_binding.contentScanResultCertainty;
        }

        public TextView get_scan_result_class() {
            return view_binding.contentScanResultClass;
        }

        public FrameLayout get_frame_layout() {
            return view_binding.frameLayout;
        }
    }
}

///**
// * The RecyclerAdapter used as interface for to the ScanResults-RecyclerView. A subclass of
// * {@link RecyclerView.Adapter}. This RecyclerView displays the Results found by the (currently
// * mocked) Computer-Vision-Pipeline for weed-detection.
// */
//public class ScanResultRecyclerAdapter extends RecyclerView.Adapter<ScanResultRecyclerAdapter.ScanResultViewHolder>{
//
//    /**
//     * the result set currently displayed in the RecyclerView
//     */
//    private List<Result> local_scan_results;
//    private Context context;
//
//    /**
//     * Constructor for ScanResultRecyclerAdapter
//     * @param scan_results the list of results to fill the RecyclerView with initially
//     * @param context the context used for assigning certain TextView using a Template
//     */
//    public ScanResultRecyclerAdapter(List<Result> scan_results, Context context) {
//        local_scan_results = scan_results;
//        this.context = context;
//    }
//
//    @NonNull
//    @org.jetbrains.annotations.NotNull
//    @Override
//    public ScanResultViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_results_row_item, parent, false);
//        return new ScanResultRecyclerAdapter.ScanResultViewHolder(view);
//    }
//
//    /**
//     * one of androids standard method called everytime the list changes and a result has to be bound
//     * to a View item
//     * 1. configures the different TextViews
//     * @param holder the view item to bind the rover to
//     * @param position the index of the holder, which equals the index of the rover in the
//     * {@link #local_scan_results}
//     */
//    @Override
//    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ScanResultRecyclerAdapter.ScanResultViewHolder holder, int position) {
//        Result result = local_scan_results.get(position);
//        holder.get_scan_result_id().setText(Integer.toString(result.result_id));
//        String certainty_string = context.getString(R.string.content_result_certainty, (int)(result.certainty * 100));
//        holder.get_scan_result_certainty().setText(certainty_string);
//    }
//
//    /**
//     * get the number of items in the RecyclerView
//     * @return number of items in the RecyclerView
//     */
//    @Override
//    public int getItemCount() {
//        return local_scan_results.size();
//    }
//
//    /**
//     * set the {@link #local_scan_results} to a new list
//     * if you don't use this function to change the set don't forget to use notifyDataSetChanged()!
//     * @param scan_results the List to set the {@link #local_scan_results} to
//     */
//    public void set_local_scan_results(List<Result> scan_results) {
//        this.local_scan_results = scan_results;
//        notifyDataSetChanged();
//    }
//
//    /**
//     * The ViewHolder for one item in the RecyclerView, which represents one result.
//     * A subclass of {@link RecyclerView.ViewHolder}
//     */
//    public static class ScanResultViewHolder extends RecyclerView.ViewHolder {
//
//        ScanResultsRowItemBinding view_binding;
//
//        public ScanResultViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
//            super(itemView);
//            view_binding = ScanResultsRowItemBinding.bind(itemView);
//        }
//
//        /**
//         * get the TextView for the result id
//         * @return TextView for the result id
//         */
//        public TextView get_scan_result_id() {
//            return view_binding.contentScanResultId;
//        }
//
//        /**
//         * get the TextView for the result certainty
//         * @return TextView for the result certainty
//         */
//        public TextView get_scan_result_certainty() {
//            return view_binding.contentScanResultCertainty;
//        }
//    }
//}
