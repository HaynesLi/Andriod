package com.paltech.dronesncars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.databinding.RoverStatusRowItemBinding;

import java.util.List;

public class RoverStatusRecyclerAdapter extends RecyclerView.Adapter<RoverStatusRecyclerAdapter.RoverStatusViewHolder> {

    private List<Rover> localRoverSet;

    public RoverStatusRecyclerAdapter(List<Rover> rover_set) {
        localRoverSet = rover_set;
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
        holder.getRoverIDText().setText(Integer.toString(rover.rid));
        holder.getRoverNameText().setText(rover.roverName);
    }

    @Override
    public int getItemCount() {
        return localRoverSet.size();
    }
}
