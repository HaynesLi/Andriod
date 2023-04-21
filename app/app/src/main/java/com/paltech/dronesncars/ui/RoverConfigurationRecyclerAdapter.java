package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverConfigurationRowItemBinding;
import com.paltech.dronesncars.model.Rover;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecyclerAdapter used as interface for to the RoverConfiguration-RecyclerView. A subclass of
 * {@link RecyclerView.Adapter}. This RecyclerView displays all usable/all connected/all rovers for
 * configuration in the {@link RoverRoutineSettingsFragment}
 */
public class RoverConfigurationRecyclerAdapter extends RecyclerView.Adapter<RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder> {

    /**
     * The rover set being displayed in the RecyclerView
     */
    private List<Rover> local_rover_set;

    /**
     * The ViewModel used to access different attributes and functions e.g. for deleting rovers from
     * the database.
     */
    private RoverRoutineSettingsViewModel view_model;
    private RecyclerView view;

    /**
     * Boolean determining whether currently all rovers are displayed (and deletable) or only the
     * connected ones
     */
    private boolean editable;

    public RoverConfigurationRecyclerAdapter(List<Rover> rover_set, RoverRoutineSettingsViewModel view_model) {
        this.local_rover_set = rover_set;
        this.view_model = view_model;
    }

    /**
     * Constructor of RoverConfigurationRecyclerAdapter
     * @param view_model the view model that will be used to access the database if necessary
     */
    public RoverConfigurationRecyclerAdapter(RoverRoutineSettingsViewModel view_model) {
        this.local_rover_set = new ArrayList<>();
        this.view_model = view_model;
    }

    public RoverConfigurationRecyclerAdapter() {
        this.local_rover_set = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public RoverConfigurationViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rover_configuration_row_item,
                parent, false);
        return new RoverConfigurationViewHolder(view);
    }

    /**
     * set {@link #editable}
     * @param editable the value to set editable to
     */
    public void setEditable(boolean editable){
        this.editable = editable;
    }

    /**
     * get {@link #editable}
     */
    public boolean getEditable(){
        return this.editable;
    }

    /**
     * one of androids standard method called everytime the list changes and a Rover has to be bound
     * to a View item
     * @param holder the view item to bind the rover to
     * @param position the index of the holder, which equals the index of the rover in the
     * {@link #local_rover_set}
     */
    @Override
    public void onBindViewHolder(@NonNull @NotNull RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder, int position) {
        Rover rover = local_rover_set.get(position);
        holder.get_rover_battery_info().setText("Battery:\t\t"+rover.battery+"%");
        holder.get_rover_name_and_id().setText(rover.roverName+" (ID: "+rover.rover_id+")");
        holder.get_rover_used_checkbox().setChecked(rover.is_used);
        set_listeners(rover, holder);
        if (editable) {
            holder.get_delete_rover_button().setVisibility(View.VISIBLE);
            holder.get_rover_used_checkbox().setVisibility(View.GONE);
        }else{
            holder.get_rover_used_checkbox().setVisibility(View.VISIBLE);
            holder.get_delete_rover_button().setVisibility(View.GONE);
        }
    }

    /**
     * configure the listeners for one item view (holder)
     * 1. get_delete_rover_button() -> delete the rover from the database
     * 2. get_rover_used_checkbox() -> set the rover used depending on the state of the checkbox
     * @param rover the rover to set the listeners for
     * @param holder the item view
     */
    private void set_listeners(Rover rover, @NonNull RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder) {

        holder.get_delete_rover_button().setOnClickListener(view -> view_model.delete_rover(rover));

        holder.get_rover_used_checkbox().setOnCheckedChangeListener((button_view, is_checked) -> {
            if(!editable) {
                view_model.set_rover_used(rover, is_checked);
            }
        });

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
     * The ViewHolder for one item in the RecyclerView, which represents one rover. A subclass of
     * {@link RecyclerView.ViewHolder}
     */
    public static class RoverConfigurationViewHolder extends RecyclerView.ViewHolder {
        RoverConfigurationRowItemBinding view_binding;

        public RoverConfigurationViewHolder(@NonNull View item_view) {
            super(item_view);
            view_binding = RoverConfigurationRowItemBinding.bind(item_view);
        }

        /**
         * get the TextView for rover name and id
         * @return the TextView for rover name and id
         */
        public TextView get_rover_name_and_id() {
            return view_binding.roverNameAndIdContent;
        }

        /**
         * get the TextView for rover battery info
         * @return the TextView for rover battery info
         */
        public TextView get_rover_battery_info(){ return view_binding.roverBatteryContent;}

        /**
         * get the Checkbox which is used to set a rover used/unused
         * @return the Checkbox which is used to set a rover used/unused
         */
        public CheckBox get_rover_used_checkbox() { return view_binding.checkBoxUseThisRover; }

        /**
         * get the ImageButton used to delete the rover
         * @return the ImageButton used to delete the rover
         */
        public ImageButton get_delete_rover_button() {
            return view_binding.buttonDeleteRover;
        }

        /**
         * the method used to adjust the view item to the current state: editable/deletable or not
         * @param editable the value to adjust the current view for
         */
        public void set_editable(boolean editable) {
            if (editable) {
                view_binding.checkBoxUseThisRover.setVisibility(View.GONE);
                view_binding.buttonDeleteRover.setVisibility(View.VISIBLE);
            } else {
                view_binding.buttonDeleteRover.setVisibility(View.GONE);
                view_binding.checkBoxUseThisRover.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * set the {@link #local_rover_set} to a new list
     * if you don't use this function to change the set don't forget to use notifyDataSetChanged()!
     * @param rovers the List to set the {@link #local_rover_set} to
     */
    public void set_local_rover_set(List<Rover> rovers) {
        this.local_rover_set = rovers;
        notifyDataSetChanged();
    }

    // TODO delete after checking it is actually not needed anymore
    public RoverConfigurationViewHolder get_holder_at_position(int position) {
        final RoverConfigurationViewHolder[] itemView = new RoverConfigurationViewHolder[1];
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout() {
                itemView[0] = (RoverConfigurationViewHolder) view.findViewHolderForAdapterPosition(position);
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return itemView[0];
    }
}
