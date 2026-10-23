package com.example.storyverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter
        extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    private final List<ChapterEntity> chapters = new ArrayList<>();

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                );

        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChapterViewHolder holder,
            int position
    ) {
        ChapterEntity chapter = chapters.get(position);

        holder.itemView.setContentDescription(
                chapter.getTitle()
        );
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void setChapters(List<ChapterEntity> newChapters) {
        chapters.clear();

        if (newChapters != null) {
            chapters.addAll(newChapters);
        }

        notifyDataSetChanged();
    }

    public static class ChapterViewHolder
            extends RecyclerView.ViewHolder {

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}