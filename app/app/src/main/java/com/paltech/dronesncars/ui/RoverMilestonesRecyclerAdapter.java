package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverMilestonesRowItemBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.Waypoint;

import java.util.List;

public class RoverMilestonesRecyclerAdapter extends RecyclerView.Adapter<RoverMilestonesRecyclerAdapter.RoverMilestonesViewHolder> {

    private List<Waypoint> localWaypointSet;
    private OnRoverMilestonesItemClickedListener clicked_listener;

    public RoverMilestonesRecyclerAdapter(List<Waypoint> waypoint_set, OnRoverMilestonesItemClickedListener clicked_listener) {
        this.localWaypointSet = waypoint_set;
        this.clicked_listener = clicked_listener;
    }

    public static class RoverMilestonesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoverMilestonesRowItemBinding view_binding;
        RoverMilestonesRecyclerAdapter.OnRoverMilestonesItemClickedListener clicked_listener;
        private Waypoint waypoint;

        public RoverMilestonesViewHolder(@NonNull View itemView, RoverMilestonesRecyclerAdapter.OnRoverMilestonesItemClickedListener clicked_listener) {
            super(itemView);
            this.clicked_listener = clicked_listener;
            this.waypoint = null;
            view_binding = RoverMilestonesRowItemBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        public TextView getMilestoneWaypointText() { return view_binding.milestoneWaypointContent; }

        public TextView getMilestoneLatitudeText() { return view_binding.milestoneLongitudeContent; }

        public TextView getMilestoneLongitudeText() { return view_binding.milestoneLongitudeContent; }

        public void setWaypoint(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        public void onClick(View v) {
            clicked_listener.onRoverMilestonesItemClicked(this.waypoint);
        }
    }

    @NonNull
    @Override
    public RoverMilestonesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rover_milestones_row_item, parent, false);
        return new RoverMilestonesViewHolder(view, clicked_listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoverMilestonesRecyclerAdapter.RoverMilestonesViewHolder holder, int position) {
        Waypoint waypoint = localWaypointSet.get(position);
        holder.getMilestoneWaypointText().setText("Waypoint "+waypoint.waypoint_number);
        holder.getMilestoneLatitudeText().setText("Latitude:\t\t"+waypoint.position.getLatitude());
        holder.getMilestoneLongitudeText().setText("Longitude:\t\t"+waypoint.position.getLongitude());
        holder.setWaypoint(waypoint);
    }

    @Override
    public int getItemCount() {
        return localWaypointSet.size();
    }

    public void setLocalWaypointSet(List<Waypoint> waypointSet) {
        this.localWaypointSet = waypointSet;
        notifyDataSetChanged();
    }

    public interface OnRoverMilestonesItemClickedListener {
        void onRoverMilestonesItemClicked(Waypoint clicked_waypoint);
    }
}
