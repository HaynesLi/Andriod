package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverStatusRowItemBinding;
import com.paltech.dronesncars.model.Rover;

import java.util.List;

public class RoverStatusRecyclerAdapter extends RecyclerView.Adapter<RoverStatusRecyclerAdapter.RoverStatusViewHolder> {

    private List<Rover> localRoverSet;

    public RoverStatusRecyclerAdapter(List<Rover> rover_set) {
        this.localRoverSet = rover_set;
    }

    public static class RoverStatusViewHolder extends RecyclerView.ViewHolder {
        RoverStatusRowItemBinding view_binding;

        public RoverStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            view_binding = RoverStatusRowItemBinding.bind(itemView);
        }

        public TextView getRoverIDText() {
            return view_binding.roverItemId;
        }

        public TextView getRoverNameText() {
            return view_binding.roverName;
        }

        public TextView getStatus() { return view_binding.roverStatus; }

        public TextView getProgress() { return view_binding.roverProgress; }
    }

    @NonNull
    @Override
    public RoverStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rover_status_row_item, parent, false);
        return new RoverStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoverStatusRecyclerAdapter.RoverStatusViewHolder holder, int position) {
        Rover rover = localRoverSet.get(position);
        holder.getRoverIDText().setText(String.format("%d", rover.rover_id));
        holder.getRoverNameText().setText(rover.roverName);
        holder.getStatus().setText(rover.status.toString());
        holder.getProgress().setText(String.format("%d%%", (int) Math.round(rover.progress * 100)));
    }

    @Override
    public int getItemCount() {
        return localRoverSet.size();
    }

    public void setLocalRoverSet(List<Rover> roverSet) {
        this.localRoverSet = roverSet;
        notifyDataSetChanged();
    }
}
