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

        public TextView getRoverNameAndIdText() {
            return view_binding.roverNameAndIdContent;
        }

        public TextView getStatus() { return view_binding.roverConnectionContent; }

        public TextView getProgress() { return view_binding.roverProgressContent; }

        public TextView getBattery() { return view_binding.roverBatteryContent; }
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
        holder.getRoverNameAndIdText().setText(rover.roverName+" (ID: "+rover.rover_id+")");
        holder.getStatus().setText("Status:\t\t"+rover.status.toString());
        holder.getProgress().setText(String.format("Progress:\t\t%d%%", (int) Math.round(rover.progress * 100)));
        holder.getBattery().setText("Battery:\t\t"+rover.battery+"%");
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
