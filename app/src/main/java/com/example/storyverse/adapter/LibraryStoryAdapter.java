package com.example.storyverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.ArrayList;
import java.util.List;

public class LibraryStoryAdapter extends RecyclerView.Adapter<LibraryStoryAdapter.LibraryViewHolder> {

    public interface OnLibraryItemListener {
        void onStoryClick(StoryEntity story);
        void onRemoveClick(StoryEntity story);
    }

    private final List<StoryEntity> stories = new ArrayList<>();
    private final OnLibraryItemListener listener;

    public LibraryStoryAdapter(OnLibraryItemListener listener) {
        this.listener = listener;
    }

    public void setStories(List<StoryEntity> newStories) {
        stories.clear();
        if (newStories != null) {
            stories.addAll(newStories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_story, parent, false);
        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        StoryEntity story = stories.get(position);
        holder.tvTitle.setText(story.getTitle() != null ? story.getTitle() : "Không có tiêu đề");
        holder.tvDescription.setText(story.getDescription() != null ? story.getDescription() : "Chưa có mô tả");
        holder.tvChapters.setText(story.getChapterCount() + " chương");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(story);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(story);
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class LibraryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvDescription, tvChapters;
        ImageButton btnRemove;

        public LibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivItemCover);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvChapters = itemView.findViewById(R.id.tvItemChapters);
            btnRemove = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}