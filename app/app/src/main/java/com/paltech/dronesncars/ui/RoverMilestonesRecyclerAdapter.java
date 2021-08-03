package com.paltech.dronesncars.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverMilestonesRowItemBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverStatus;
import com.paltech.dronesncars.model.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        public LinearLayout getLayoutMilestoneItem() { return view_binding.layoutMilestonesRowItem; }

        public TextView getMilestoneWaypointText() { return view_binding.milestoneWaypointContent; }

        public TextView getMilestoneLatitudeText() { return view_binding.milestoneLatitudeContent; }

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
        holder.getMilestoneLatitudeText().setText("Latitude:\t\t\t"+String.format("%.9g%n", waypoint.position.getLatitude()));
        holder.getMilestoneLongitudeText().setText("Longitude:\t\t"+String.format("%.9g%n", waypoint.position.getLongitude()));
        holder.setWaypoint(waypoint);
        String pathToWaypointData = holder.getLayoutMilestoneItem().getContext().getFilesDir()+"/Milestones/Mission_"+waypoint.mission_id+"/Waypoint_"+waypoint.waypoint_number+"/waypoint_data.json";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(pathToWaypointData));
            String jsonString = new String(encoded, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(jsonString);
            String errors = jsonObject.getString("errors");
            if(errors.equals("")){
                holder.getLayoutMilestoneItem().setBackgroundResource(R.drawable.rectangle_green);
            }else{
                holder.getLayoutMilestoneItem().setBackgroundResource(R.drawable.rectangle_red);
            }
        } catch (IOException | JSONException e) {
            holder.getLayoutMilestoneItem().setBackgroundResource(R.drawable.rectangle_red);
        }
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
