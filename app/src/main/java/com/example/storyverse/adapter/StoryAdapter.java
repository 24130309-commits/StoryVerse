
package com.example.storyverse.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.model.Story;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private final List<StoryEntity> localStories = new ArrayList<>();
    private final List<Story> apiStories = new ArrayList<>();

    private boolean isApiData = false;

    private OnStoryClickListener listener;

    private final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    public interface OnStoryClickListener {
        void onApiStoryClick(Story story);

        void onLocalStoryClick(StoryEntity story);
    }

    public StoryAdapter() {
    }

    public StoryAdapter(OnStoryClickListener listener) {
        this.listener = listener;
    }

    public void setOnStoryClickListener(OnStoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story, parent, false);

        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StoryViewHolder holder,
            int position
    ) {
        if (isApiData) {
            Story story = apiStories.get(position);

            holder.tvTitle.setText(
                    story.getTitle() != null
                            ? story.getTitle()
                            : "Chưa có tên truyện"
            );

            holder.tvAuthor.setText(
                    "Tác giả: " + safeText(story.getAuthor(), "Chưa rõ")
            );

            holder.tvDescription.setText(
                    safeText(story.getDescription(), "Chưa có mô tả")
            );

            holder.tvSource.setText(
                    "Nguồn: " + safeText(story.getSource(), "Không rõ")
            );

            loadCover(holder.ivCover, story.getCoverUrl());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApiStoryClick(story);
                }
            });

        } else {
            StoryEntity story = localStories.get(position);

            holder.tvTitle.setText(
                    safeText(story.getTitle(), "Chưa có tên truyện")
            );

            holder.tvAuthor.setText(
                    "Tác giả ID: " + safeText(story.getAuthorId(), "Chưa rõ")
            );

            holder.tvDescription.setText(
                    safeText(story.getDescription(), "Chưa có mô tả")
            );

            holder.tvSource.setText(
                    "Nguồn: " + safeText(story.getSource(), "Thư viện")
            );

            loadCover(holder.ivCover, story.getCoverUrl());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocalStoryClick(story);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return isApiData ? apiStories.size() : localStories.size();
    }

    // Dữ liệu truyện lấy từ Room - giữ tương thích với code cũ.
    public void setStories(List<StoryEntity> newStories) {
        isApiData = false;

        localStories.clear();

        if (newStories != null) {
            localStories.addAll(newStories);
        }

        notifyDataSetChanged();
    }

    // Dữ liệu truyện lấy từ Google Books và Open Library.
    public void setApiStories(List<Story> newStories) {
        isApiData = true;

        apiStories.clear();

        if (newStories != null) {
            apiStories.addAll(newStories);
        }

        notifyDataSetChanged();
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private void loadCover(ImageView imageView, String imageUrl) {

        imageView.setImageResource(R.drawable.ic_launcher_background);

        imageView.setTag(imageUrl);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        imageExecutor.execute(() -> {

            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(imageUrl);

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                }

            } catch (Exception e) {
                android.util.Log.e(
                        "StoryAdapter",
                        "Không thể tải ảnh bìa",
                        e
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            if (bitmap != null) {
                Bitmap finalBitmap = bitmap;

                imageView.post(() -> {
                    Object tag = imageView.getTag();

                    if (imageUrl.equals(tag)) {
                        imageView.setImageBitmap(finalBitmap);
                    }
                });
            }
        });
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvDescription;
        TextView tvSource;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.iv_story_cover);
            tvTitle = itemView.findViewById(R.id.tv_story_title);
            tvAuthor = itemView.findViewById(R.id.tv_story_author);
            tvDescription = itemView.findViewById(R.id.tv_story_description);
            tvSource = itemView.findViewById(R.id.tv_story_source);
        }
    }
}
