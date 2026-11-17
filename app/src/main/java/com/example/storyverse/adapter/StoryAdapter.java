package com.example.storyverse.adapter;

import android.content.Context;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    public static final int MODE_HORIZONTAL = 0;
    public static final int MODE_VERTICAL = 1;

    private final List<StoryEntity> localStories = new ArrayList<>();
    private final List<Story> apiStories = new ArrayList<>();

    private final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    private final Map<String, String> authorNames = new HashMap<>();
    private final Map<String, String> storyGenres = new HashMap<>();

    private boolean isApiData = false;
    private boolean useHomeLayout = false;
    private int displayMode = MODE_VERTICAL;

    private OnStoryClickListener listener;
    private OnLocalStoryClickListener localClickListener;
    private OnApiStoryItemClickListener apiItemClickListener;

    public interface OnStoryClickListener {
        void onApiStoryClick(Story story);

        void onLocalStoryClick(StoryEntity story);
    }

    public interface OnLocalStoryClickListener {
        void onStoryClick(StoryEntity story);
    }

    public interface OnApiStoryItemClickListener {
        void onStoryClick(Story story);
    }

    public StoryAdapter() {
    }

    // Dùng cho Reader và Search.
    public StoryAdapter(OnStoryClickListener listener) {
        this.listener = listener;
    }

    // Dùng cho Home và Explore.
    public StoryAdapter(
            int displayMode,
            OnLocalStoryClickListener listener
    ) {
        this.displayMode = displayMode;
        this.localClickListener = listener;
        this.useHomeLayout = true;
    }

    // Dùng cho luồng tìm kiếm cũ nhận danh sách Story.
    public StoryAdapter(
            Context context,
            List<Story> filteredStories,
            OnApiStoryItemClickListener listener
    ) {
        this.apiItemClickListener = listener;

        if (filteredStories != null) {
            apiStories.addAll(filteredStories);
        }

        isApiData = true;
    }

    public void setOnStoryClickListener(OnStoryClickListener listener) {
        this.listener = listener;
    }

    public void setLocalStoryClickListener(
            OnLocalStoryClickListener listener
    ) {
        this.localClickListener = listener;
    }

    public void setApiStoryItemClickListener(
            OnApiStoryItemClickListener listener
    ) {
        this.apiItemClickListener = listener;
    }

    public void setLocalMetadata(
            Map<String, String> authors,
            Map<String, String> genres
    ) {
        authorNames.clear();
        storyGenres.clear();

        if (authors != null) {
            authorNames.putAll(authors);
        }

        if (genres != null) {
            storyGenres.putAll(genres);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        int layoutRes;

        if (!useHomeLayout) {
            layoutRes = R.layout.item_story;
        } else if (displayMode == MODE_HORIZONTAL) {
            layoutRes = R.layout.item_story_featured;
        } else {
            layoutRes = R.layout.item_story_vertical;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);

        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StoryViewHolder holder,
            int position
    ) {
        if (isApiData) {
            if (position < 0 || position >= apiStories.size()) {
                return;
            }

            bindApiStory(holder, apiStories.get(position));

        } else {
            if (position < 0 || position >= localStories.size()) {
                return;
            }

            bindLocalStory(holder, localStories.get(position));
        }
    }

    private void bindApiStory(
            StoryViewHolder holder,
            Story story
    ) {
        if (story == null) {
            return;
        }

        loadCover(holder.ivCover, story.getCoverUrl());

        setText(holder.tvTitle, story.getTitle());

        if (useHomeLayout) {
            String category = story.getCategory();

            if (category == null || category.trim().isEmpty()) {
                category = getFirstCategory(story.getCategories());
            }

            setText(holder.tvGenre, category);
            setText(holder.tvAuthor, story.getAuthor());
            setText(holder.tvDescription, story.getDescription());
            setText(holder.tvStats, "");

        } else {
            setText(holder.tvTitle, story.getTitle());
            setText(holder.tvAuthor, story.getAuthor());
            setText(holder.tvDescription, story.getDescription());
            setText(holder.tvSource, story.getSource());
        }

        holder.itemView.setContentDescription(story.getTitle());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (apiItemClickListener != null) {
                apiItemClickListener.onStoryClick(story);
            }

            if (listener != null) {
                listener.onApiStoryClick(story);
            }
        });
    }

    private void bindLocalStory(
            StoryViewHolder holder,
            StoryEntity story
    ) {
        if (story == null) {
            return;
        }

        loadCover(holder.ivCover, story.getCoverUrl());

        String storyId = story.getStoryId();

        String authorName = authorNames.get(story.getAuthorId());

        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = "Đang cập nhật";
        }

        String genre = storyGenres.get(storyId);

        if (genre == null || genre.trim().isEmpty()) {
            genre = "Chưa phân loại";
        }

        setText(holder.tvTitle, story.getTitle());

        if (useHomeLayout) {
            setText(holder.tvGenre, genre);
            setText(holder.tvAuthor, authorName);
            setText(holder.tvDescription, story.getDescription());

            String stats = story.getViewCount()
                    + " lượt xem • "
                    + story.getChapterCount()
                    + " chương";

            setText(holder.tvStats, stats);

        } else {
            setText(holder.tvAuthor, authorName);
            setText(holder.tvDescription, story.getDescription());
            setText(holder.tvSource, story.getSource());
        }

        holder.itemView.setContentDescription(story.getTitle());

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (localClickListener != null) {
                localClickListener.onStoryClick(story);
            }

            if (listener != null) {
                listener.onLocalStoryClick(story);
            }
        });
    }

    private String getFirstCategory(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }

        String category = categories.get(0);

        return category == null ? "" : category;
    }

    private void setText(TextView textView, String value) {
        if (textView != null) {
            textView.setText(
                    value == null || value.trim().isEmpty()
                            ? ""
                            : value
            );
        }
    }

    private void loadCover(ImageView imageView, String imageUrl) {
        if (imageView == null) {
            return;
        }

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

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = connection.getInputStream()) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }
                }

            } catch (Exception e) {
                bitmap = null;

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            Bitmap finalBitmap = bitmap;

            if (finalBitmap != null) {
                imageView.post(() -> {
                    Object currentTag = imageView.getTag();

                    if (imageUrl.equals(currentTag)) {
                        imageView.setImageBitmap(finalBitmap);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return isApiData ? apiStories.size() : localStories.size();
    }

    public void setStories(List<StoryEntity> stories) {
        isApiData = false;

        localStories.clear();

        if (stories != null) {
            localStories.addAll(stories);
        }

        notifyDataSetChanged();
    }

    public void setApiStories(List<Story> stories) {
        isApiData = true;

        apiStories.clear();

        if (stories != null) {
            apiStories.addAll(stories);
        }

        notifyDataSetChanged();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCover;

        TextView tvTitle;
        TextView tvAuthor;
        TextView tvDescription;
        TextView tvSource;
        TextView tvGenre;
        TextView tvStats;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.iv_story_cover);

            if (ivCover == null) {
                ivCover = itemView.findViewById(R.id.imgStoryCover);
            }

            tvTitle = itemView.findViewById(R.id.tv_story_title);

            if (tvTitle == null) {
                tvTitle = itemView.findViewById(R.id.tvStoryTitle);
            }

            tvAuthor = itemView.findViewById(R.id.tv_story_author);

            if (tvAuthor == null) {
                tvAuthor = itemView.findViewById(R.id.tvStoryAuthor);
            }

            tvDescription = itemView.findViewById(R.id.tv_story_description);

            if (tvDescription == null) {
                tvDescription = itemView.findViewById(R.id.tvStoryDesc);
            }

            tvSource = itemView.findViewById(R.id.tv_story_source);
            tvGenre = itemView.findViewById(R.id.tvStoryGenre);
            tvStats = itemView.findViewById(R.id.tvStoryStats);
        }
    }
}