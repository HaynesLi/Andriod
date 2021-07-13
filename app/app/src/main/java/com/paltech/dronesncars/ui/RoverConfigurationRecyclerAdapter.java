package com.paltech.dronesncars.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.paltech.dronesncars.R;
import com.paltech.dronesncars.databinding.RoverConfigurationRowItemBinding;
import com.paltech.dronesncars.model.Rover;
import com.paltech.dronesncars.model.RoverStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RoverConfigurationRecyclerAdapter extends RecyclerView.Adapter<RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder> {

    private List<Rover> local_rover_set;
    private RoverRoutineSettingsViewModel view_model;
    private RecyclerView view;
    private boolean editable;

    public RoverConfigurationRecyclerAdapter(List<Rover> rover_set, RoverRoutineSettingsViewModel view_model) {
        this.local_rover_set = rover_set;
        this.view_model = view_model;
    }

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

    public void setEditable(boolean editable){
        this.editable = editable;
    }

    public boolean getEditable(){
        return this.editable;
    }

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

    private void set_listeners(Rover rover, @NonNull RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder) {

        holder.get_delete_rover_button().setOnClickListener(view -> view_model.delete_rover(rover));

        holder.get_rover_used_checkbox().setOnCheckedChangeListener((button_view, is_checked) -> {
            if(!editable) {
                view_model.set_rover_used(rover, is_checked);
            }
        });

    }

    @Override
    public int getItemCount() {
        return local_rover_set.size();
    }

    public static class RoverConfigurationViewHolder extends RecyclerView.ViewHolder {
        RoverConfigurationRowItemBinding view_binding;

        public RoverConfigurationViewHolder(@NonNull View item_view) {
            super(item_view);
            view_binding = RoverConfigurationRowItemBinding.bind(item_view);
        }

        public TextView get_rover_name_and_id() {
            return view_binding.roverNameAndIdContent;
        }

        public TextView get_rover_battery_info(){ return view_binding.roverBatteryContent;}

        public CheckBox get_rover_used_checkbox() { return view_binding.checkBoxUseThisRover; }

        public ImageButton get_delete_rover_button() {
            return view_binding.buttonDeleteRover;
        }

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

    public void set_local_rover_set(List<Rover> rovers) {
        this.local_rover_set = rovers;
        notifyDataSetChanged();
    }

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
