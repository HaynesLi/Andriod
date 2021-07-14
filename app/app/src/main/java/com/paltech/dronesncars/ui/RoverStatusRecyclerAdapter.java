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
    private OnRoverStatusItemClickedListener clicked_listener;

    public RoverStatusRecyclerAdapter(List<Rover> rover_set, OnRoverStatusItemClickedListener clicked_listener) {
        this.localRoverSet = rover_set;
        this.clicked_listener = clicked_listener;
    }

    public static class RoverStatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoverStatusRowItemBinding view_binding;
        OnRoverStatusItemClickedListener clicked_listener;
        private Rover rover;

        public RoverStatusViewHolder(@NonNull View itemView, OnRoverStatusItemClickedListener clicked_listener) {
            super(itemView);
            this.clicked_listener = clicked_listener;
            this.rover = null;
            view_binding = RoverStatusRowItemBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        public TextView getRoverIDText() {
            return view_binding.roverItemId;
        }

        public TextView getRoverNameText() {
            return view_binding.roverName;
        }

        public TextView getStatus() { return view_binding.roverStatus; }

        public TextView getProgress() { return view_binding.roverProgress; }

        public void setRover(Rover rover) {
            this.rover = rover;
        }

        public Rover getRover() {
            return this.rover;
        }

        @Override
        public void onClick(View v) {
            clicked_listener.onRoverStatusItemClicked(this.rover);
        }
    }

    @NonNull
    @Override
    public RoverStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rover_status_row_item, parent, false);

        return new RoverStatusViewHolder(view, clicked_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoverStatusRecyclerAdapter.RoverStatusViewHolder holder, int position) {
        Rover rover = localRoverSet.get(position);
        holder.getRoverIDText().setText(String.format("%d", rover.rover_id));
        holder.getRoverNameText().setText(rover.roverName);
        holder.getStatus().setText(rover.status.toString());
        holder.getProgress().setText(String.format("%d%%", (int) Math.round(rover.progress * 100)));
        holder.setRover(rover);
    }

    @Override
    public int getItemCount() {
        return localRoverSet.size();
    }

    public void setLocalRoverSet(List<Rover> roverSet) {
        this.localRoverSet = roverSet;
        notifyDataSetChanged();
    }

    public interface OnRoverStatusItemClickedListener {
        void onRoverStatusItemClicked(Rover clicked_rover);
    }
}
