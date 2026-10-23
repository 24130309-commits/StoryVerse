package com.example.storyverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.data.local.entity.NotificationEntity;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<NotificationEntity> notifications = new ArrayList<>();

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                );

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder,
            int position
    ) {
        NotificationEntity notification = notifications.get(position);

        holder.itemView.setContentDescription(
                notification.getTitle()
        );
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(
            List<NotificationEntity> newNotifications
    ) {
        notifications.clear();

        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }

        notifyDataSetChanged();
    }

    public static class NotificationViewHolder
            extends RecyclerView.ViewHolder {

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}