package com.paltech.dronesncars.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class RoverConfigurationRecyclerAdapter extends RecyclerView.Adapter<RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder> {

    private List<Rover> local_rover_set;

    public RoverConfigurationRecyclerAdapter(List<Rover> rover_set) {
        this.local_rover_set = rover_set;
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

    @Override
    public void onBindViewHolder(@NonNull @NotNull RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder, int position) {
        Rover rover = local_rover_set.get(position);
        holder.get_rover_id_text().setText(String.format("%d", rover.rover_id));
        holder.get_rover_status().setText(rover.status.toString());
        set_listeners(rover, holder);
    }

    private void set_listeners(Rover rover, @NonNull RoverConfigurationRecyclerAdapter.RoverConfigurationViewHolder holder) {
        holder.get_connection_test_button().setOnClickListener(view -> connection_test());

        holder.get_delete_rover_button().setOnClickListener(view -> {
            local_rover_set.remove(rover);
            notifyDataSetChanged();
        });

        holder.get_rover_id_text().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ip = s.toString();
                change_ip_in_viewmodel(ip, rover);
            }
        });
    }

    // TODO
    private void change_ip_in_viewmodel(String ip, Rover rover) {}

    // TODO
    private void connection_test() {}

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

        public TextView get_rover_id_text() {
            return view_binding.roverIdContent;
        }

        public EditText get_rover_ip_text() {
            return view_binding.roverIpContent;
        }

        public Button get_connection_test_button() {
            return view_binding.buttonTestConnection;
        }

        public ImageButton get_delete_rover_button() {
            return view_binding.buttonDeleteRover;
        }

        public TextView get_rover_status() {
            return view_binding.connectionTestResult;
        }
    }
}
