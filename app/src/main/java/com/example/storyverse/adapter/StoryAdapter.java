package com.example.storyverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter
        extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<StoryEntity> stories = new ArrayList<>();

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                );

        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StoryViewHolder holder,
            int position
    ) {
        StoryEntity story = stories.get(position);

        holder.itemView.setContentDescription(
                story.getTitle()
        );
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public void setStories(List<StoryEntity> newStories) {
        stories.clear();

        if (newStories != null) {
            stories.addAll(newStories);
        }

        notifyDataSetChanged();
    }

    public static class StoryViewHolder
            extends RecyclerView.ViewHolder {

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}