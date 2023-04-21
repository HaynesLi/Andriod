package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverStatusRowItemBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverStatus;

import java.util.List;

/**
 * The RecyclerAdapter used as interface for to the RoverStatus-RecyclerView. A subclass of
 * {@link RecyclerView.Adapter}. This RecyclerView displays the different used rovers in the
 * {@link RoverStatusFragment}.
 */
public class RoverStatusRecyclerAdapter extends RecyclerView.Adapter<RoverStatusRecyclerAdapter.RoverStatusViewHolder> {

    /**
     * the rover set currently displayed in the RecyclerView
     */
    private List<Rover> local_rover_set;

    /**
     * the clicked listener used to allow for "external" reactions when a single RecyclerView item
     * is clicked -> in our case the fragment {@link RoverStatusFragment} containing the
     * RecyclerView is implementing this interface!
     */
    private OnRoverStatusItemClickedListener clicked_listener;

    /**
     * The constructor for the RoverStatusRecyclerAdapter
     * @param rover_set the rover set to fill the RecyclerView with initially
     * @param clicked_listener the clicked listener used to realize onClick-events on a single
     *                         list-item
     */
    public RoverStatusRecyclerAdapter(List<Rover> rover_set, OnRoverStatusItemClickedListener clicked_listener) {
        this.local_rover_set = rover_set;
        this.clicked_listener = clicked_listener;
    }

    /**
     * The ViewHolder for one item in the RecyclerView, which represents one rover.
     * A subclass of {@link RecyclerView.ViewHolder}
     */
    public static class RoverStatusViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoverStatusRowItemBinding view_binding;
        OnRoverStatusItemClickedListener clicked_listener;
        /**
         * The rover this RoverStatusViewHolder represents
         */
        private Rover rover;

        public RoverStatusViewHolder(@NonNull View itemView, OnRoverStatusItemClickedListener clicked_listener) {
            super(itemView);
            this.clicked_listener = clicked_listener;
            this.rover = null;
            view_binding = RoverStatusRowItemBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        /**
         * get the LinearLayout containing the item to set the background color
         * @return LinearLayout containing the item to set the background color
         */
        public LinearLayout getLayoutStatusItem() { return view_binding.layoutStatusRowItem; }

        /**
         * get the TextView for the rover name and id
         * @return TextView for the rover name and id
         */
        public TextView getRoverNameAndIdText() {
            return view_binding.roverNameAndIdContent;
        }

        /**
         * get the TextView for the rover status
         * @return TextView for the rover status
         */
        public TextView getStatus() { return view_binding.roverConnectionContent; }

        /**
         * get the TextView for the progress
         * @return TextView for the progress
         */
        public TextView getProgress() { return view_binding.roverProgressContent; }

        /**
         * get the TextView for the battery status
         * @return TextView for the battery status
         */
        public TextView getBattery() { return view_binding.roverBatteryContent; }

        public void setRover(Rover rover) {
            this.rover = rover;
        }

        public Rover getRover() {
            return this.rover;
        }

        /**
         * the onClick Method of one single item -> references the {@link #clicked_listener}
         * specified for this purpose
         * @param v the clicked view item
         */
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

    /**
     * one of androids standard method called everytime the list changes and a Rover has to be bound
     * to a View item
     * 1. configures the different TextViews
     * 2. configures the background
     * @param holder the view item to bind the rover to
     * @param position the index of the holder, which equals the index of the rover in the
     * {@link #local_rover_set}
     */
    @Override
    public void onBindViewHolder(@NonNull RoverStatusRecyclerAdapter.RoverStatusViewHolder holder, int position) {
        Rover rover = local_rover_set.get(position);
        holder.getRoverNameAndIdText().setText(rover.roverName+" (ID: "+rover.rover_id+")");
        holder.getStatus().setText("Status:\t\t"+rover.status.toString());
        holder.getProgress().setText(String.format("Progress:\t\t%d%%", (int) Math.round(rover.progress * 100)));
        holder.getBattery().setText("Battery:\t\t"+rover.battery+"%");
        if(rover.status == RoverStatus.DISCONNECTED){
            holder.getLayoutStatusItem().setBackgroundResource(R.drawable.rectangle_red);
        }else  {
            holder.getLayoutStatusItem().setBackgroundResource(R.drawable.rectangle_green);
        }
        holder.setRover(rover);
    }

    /**
     * get the number of items in the RecyclerView
     * @return number of items in the RecyclerView
     */
    @Override
    public int getItemCount() {
        return local_rover_set.size();
    }

    /**
     * set the {@link #local_rover_set} to a new list
     * if you don't use this function to change the set don't forget to use notifyDataSetChanged()!
     * @param rover_set the List to set the {@link #local_rover_set} to
     */
    public void set_local_rover_set(List<Rover> rover_set) {
        this.local_rover_set = rover_set;
        notifyDataSetChanged();
    }

    /**
     * The interface a class has to implement in order to be able to be used as the
     * "executing agency" for the ClickedListener
     */
    public interface OnRoverStatusItemClickedListener {
        void onRoverStatusItemClicked(Rover clicked_rover);
    }
}
