package com.example.storyverse.adapter; // Sửa lại đúng package hiện tại của file StoryAdapter

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.model.Story;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    public static final int MODE_HORIZONTAL = 0;
    public static final int MODE_VERTICAL = 1;

    private final List<StoryEntity> stories = new ArrayList<>();
    private int displayMode = MODE_VERTICAL;
    private OnStoryClickListener listener;

    public StoryAdapter(Context context, List<Story> filteredStories, Object o) {
    }

    public interface OnStoryClickListener {
        void onStoryClick(StoryEntity story);
    }

    // Constructor rỗng (đảm bảo không lỗi nếu ExploreFragment/SearchFragment gọi new StoryAdapter())
    public StoryAdapter() {
    }

    public StoryAdapter(int displayMode, OnStoryClickListener listener) {
        this.displayMode = displayMode;
        this.listener = listener;
    }

    public void setOnStoryClickListener(OnStoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (displayMode == MODE_HORIZONTAL)
                ? R.layout.item_story_featured
                : R.layout.item_story_vertical;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        StoryEntity story = stories.get(position);
        Context context = holder.itemView.getContext();

        holder.itemView.setContentDescription(story.getTitle());
        holder.tvTitle.setText(story.getTitle());

        // TODO: Mở comment các dòng dưới đây nếu StoryEntity.java của nhóm có sẵn các hàm get tương ứng:
        /*
        if (holder.tvAuthor != null && story.getAuthor() != null) {
            holder.tvAuthor.setText("Tác giả: " + story.getAuthor());
        }
        if (holder.tvGenre != null && story.getGenre() != null) {
            holder.tvGenre.setText(story.getGenre());
        }
        if (holder.tvDesc != null && story.getDescription() != null) {
            holder.tvDesc.setText(story.getDescription());
        }
        if (story.getCoverUrl() != null) {
            Glide.with(context)
                    .load(story.getCoverUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgCover);
        }
        */

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoryClick(story);
            }
        });
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

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvGenre, tvAuthor, tvDesc, tvStats;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgStoryCover);
            tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            tvGenre = itemView.findViewById(R.id.tvStoryGenre);
            tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
            tvDesc = itemView.findViewById(R.id.tvStoryDesc);
            tvStats = itemView.findViewById(R.id.tvStoryStats);
        }
    }
}