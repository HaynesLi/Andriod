package com.paltech.dronesncars.ui;

;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.ScanResultsRowItemBinding;
import com.paltech.dronesncars.model.Result;

import java.util.List;

public class ScanResultRecyclerAdapter extends RecyclerView.Adapter<ScanResultRecyclerAdapter.ScanResultViewHolder>{

    private List<Result> local_scan_results;
    private Context context;

    public ScanResultRecyclerAdapter(List<Result> scan_results, Context context) {
        local_scan_results = scan_results;
        this.context = context;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ScanResultViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_results_row_item, parent, false);
        return new ScanResultRecyclerAdapter.ScanResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ScanResultRecyclerAdapter.ScanResultViewHolder holder, int position) {
        Result result = local_scan_results.get(position);
        holder.get_scan_result_id().setText(Integer.toString(result.result_id));
        String certainty_string = context.getString(R.string.content_result_certainty, (int)(result.certainty * 100));
        holder.get_scan_result_certainty().setText(certainty_string);
    }

    @Override
    public int getItemCount() {
        return local_scan_results.size();
    }

    public void set_local_scan_results(List<Result> scan_results) {
        this.local_scan_results = scan_results;
        notifyDataSetChanged();
    }

    public static class ScanResultViewHolder extends RecyclerView.ViewHolder {

        ScanResultsRowItemBinding view_binding;

        public ScanResultViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            view_binding = ScanResultsRowItemBinding.bind(itemView);
        }

        public TextView get_scan_result_id() {
            return view_binding.contentScanResultId;
        }

        public TextView get_scan_result_certainty() {
            return view_binding.contentScanResultCertainty;
        }
    }
}
