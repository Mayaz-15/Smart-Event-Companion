package com.example.smarteventcompanion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RegisteredEventAdapter extends RecyclerView.Adapter<RegisteredEventAdapter.ViewHolder> {

    private final List<Event> events;

    public RegisteredEventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registered_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.txtName.setText(event.getName());
        holder.txtDate.setText(event.getDate());
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDate;
        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.itemRegName);
            txtDate = itemView.findViewById(R.id.itemRegDate);
        }
    }
}
