package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverMilestonesRowItemBinding;
import com.paltech.dronesncars.model.Waypoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The RecyclerAdapter used as interface for to the RoverMilestones-RecyclerView. A subclass of
 * {@link RecyclerView.Adapter}. This RecyclerView displays the Milestones a selected rover in the
 * {@link RoverStatusFragment} already has reached.
 */
public class RoverMilestonesRecyclerAdapter extends RecyclerView.Adapter<RoverMilestonesRecyclerAdapter.RoverMilestonesViewHolder> {

    /**
     * the waypoint set currently displayed in the RecyclerView
     */
    private List<Waypoint> local_waypoint_set;

    /**
     * the clicked listener used to allow for "external" reactions when a single RecyclerView item
     * is clicked -> in our case the fragment {@link RoverStatusFragment} containing the
     * RecyclerView is implementing this interface!
     */
    private OnRoverMilestonesItemClickedListener clicked_listener;

    /**
     * The Constructor for the RoverMilestonesRecyclerAdapter
     * @param waypoint_set the waypoint set to fil the RecyclerView with initially
     * @param clicked_listener the clicked listener to use
     */
    public RoverMilestonesRecyclerAdapter(List<Waypoint> waypoint_set, OnRoverMilestonesItemClickedListener clicked_listener) {
        this.local_waypoint_set = waypoint_set;
        this.clicked_listener = clicked_listener;
    }

    /**
     * The ViewHolder for one item in the RecyclerView, which represents one Waypoint/Milestone.
     * A subclass of {@link RecyclerView.ViewHolder}
     */
    public static class RoverMilestonesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoverMilestonesRowItemBinding view_binding;
        /**
         * the clicked listener that is used to realize external onClick-events on one single
         * item in the RecyclerView
         */
        RoverMilestonesRecyclerAdapter.OnRoverMilestonesItemClickedListener clicked_listener;
        private Waypoint waypoint;

        public RoverMilestonesViewHolder(@NonNull View itemView, RoverMilestonesRecyclerAdapter.OnRoverMilestonesItemClickedListener clicked_listener) {
            super(itemView);
            this.clicked_listener = clicked_listener;
            this.waypoint = null;
            view_binding = RoverMilestonesRowItemBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        /**
         * get the LinearLayout for the Milestone-Row-Item.
         * Used for changing the color of the item based on the waypoint information
         * @return the LinearLayout
         */
        public LinearLayout getLayoutMilestoneItem() { return view_binding.layoutMilestonesRowItem; }

        /**
         * get the TextView to display the waypoint number of the corresponding item
         * @return the TextView
         */
        public TextView getMilestoneWaypointText() { return view_binding.milestoneWaypointContent; }

        /**
         * get the TextView to display the latitude of the corresponding waypoint
         * @return the TextView
         */
        public TextView getMilestoneLatitudeText() { return view_binding.milestoneLatitudeContent; }

        /**
         * get the TextView to display the longitude of the corresponding waypoint
         * @return the TextView
         */
        public TextView getMilestoneLongitudeText() { return view_binding.milestoneLongitudeContent; }

        public void setWaypoint(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        /**
         * the onClick Method of one single item -> references the {@link #clicked_listener}
         * specified for this purpose
         * @param v the clicked view item
         */
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

    /**
     * one of androids standard method called everytime the list changes and a Waypoint has to be
     * bound to a View item
     * Sets the text input of every View item based on the corresponding waypoint.
     * Reads the waypoint_info file to check if the file exists and the error message is empty (in this case the item has color green, otherwise red)
     * @param holder the view item to bind the rover to
     * @param position the index of the holder, which equals the index of the rover in the
     * {@link #local_waypoint_set}
     */
    @Override
    public void onBindViewHolder(@NonNull RoverMilestonesRecyclerAdapter.RoverMilestonesViewHolder holder, int position) {
        Waypoint waypoint = local_waypoint_set.get(position);
        holder.getMilestoneWaypointText().setText("Waypoint "+waypoint.waypoint_number);
        holder.getMilestoneLatitudeText().setText("Latitude:\t\t\t"+String.format("%.9g%n", waypoint.position.getLatitude()));
        holder.getMilestoneLongitudeText().setText("Longitude:\t\t"+String.format("%.9g%n", waypoint.position.getLongitude()));
        holder.setWaypoint(waypoint);
        String pathToWaypointData = holder.getLayoutMilestoneItem().getContext().getFilesDir()+"/missions/mission_"+waypoint.mission_id+"/waypoint_"+waypoint.waypoint_number+"/waypoint_data.json";
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

    /**
     * get the number of items in the RecyclerView
     * @return number of items in the RecyclerView
     */
    @Override
    public int getItemCount() {
        return local_waypoint_set.size();
    }

    /**
     * set the {@link #local_waypoint_set} to a new list
     * if you don't use this function to change the set don't forget to use notifyDataSetChanged()!
     * @param waypoint_set the List to set the {@link #local_waypoint_set} to
     */
    public void set_local_waypoint_set(List<Waypoint> waypoint_set) {
        this.local_waypoint_set = waypoint_set;
        notifyDataSetChanged();
    }

    /**
     * The interface a class has to implement in order to be able to be used as the
     * "executing agency" for the ClickedListener
     */
    public interface OnRoverMilestonesItemClickedListener {
        void onRoverMilestonesItemClicked(Waypoint clicked_waypoint);
    }
}
