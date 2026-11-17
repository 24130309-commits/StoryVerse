
package com.example.storyverse.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.data.local.entity.StoryEntity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryStoryAdapter
        extends RecyclerView.Adapter<LibraryStoryAdapter.LibraryViewHolder> {

    public static final int MODE_FAVORITES = 0;
    public static final int MODE_READING = 1;
    public static final int MODE_HISTORY = 2;

    private static final int PLACEHOLDER =
            android.R.drawable.ic_menu_gallery;

    private static final ExecutorService imageExecutor =
            Executors.newFixedThreadPool(3);

    private static final int MAX_CACHE_SIZE =
            (int) (Runtime.getRuntime().maxMemory() / 8);

    private static final LruCache<String, Bitmap> imageCache =
            new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
                @Override
                protected int sizeOf(
                        @NonNull String key,
                        @NonNull Bitmap bitmap
                ) {
                    return bitmap.getByteCount();
                }
            };

    private final List<StoryEntity> stories = new ArrayList<>();
    private final OnLibraryItemListener listener;

    private final Map<String, ReadingProgressEntity> readingProgressMap =
            new LinkedHashMap<>();

    private final Map<String, Long> historyReadAtMap =
            new LinkedHashMap<>();

    private int displayMode = MODE_FAVORITES;

    public interface OnLibraryItemListener {
        void onStoryClick(StoryEntity story);

        void onRemoveClick(StoryEntity story);
    }

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

    public void setDisplayMode(int mode) {
        if (displayMode != mode) {
            displayMode = mode;
            notifyDataSetChanged();
        }
    }

    public void setReadingProgressMap(
            Map<String, ReadingProgressEntity> progressMap
    ) {
        readingProgressMap.clear();

        if (progressMap != null) {
            readingProgressMap.putAll(progressMap);
        }

        if (displayMode == MODE_READING) {
            notifyDataSetChanged();
        }
    }

    public void setHistoryReadAtMap(Map<String, Long> readAtMap) {
        historyReadAtMap.clear();

        if (readAtMap != null) {
            historyReadAtMap.putAll(readAtMap);
        }

        if (displayMode == MODE_HISTORY) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_story, parent, false);

        return new LibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull LibraryViewHolder holder,
            int position
    ) {
        StoryEntity story = stories.get(position);

        String storyId = story.getStoryId();

        String title = story.getTitle();
        String description = story.getDescription();

        holder.tvTitle.setText(
                title != null && !title.trim().isEmpty()
                        ? title
                        : "Không có tiêu đề"
        );

        holder.tvDescription.setText(
                description != null && !description.trim().isEmpty()
                        ? description
                        : "Chưa có mô tả"
        );

        holder.tvChapters.setText(
                story.getChapterCount() + " chương"
        );

        loadCover(holder.ivCover, story.getCoverUrl());

        holder.tvRating.setVisibility(
                displayMode == MODE_FAVORITES
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.progressReading.setVisibility(View.GONE);
        holder.tvMeta.setVisibility(View.GONE);

        holder.btnRemove.setVisibility(
                displayMode == MODE_FAVORITES
                        ? View.VISIBLE
                        : View.GONE
        );

        switch (displayMode) {
            case MODE_READING:
                bindReadingInfo(holder, storyId);
                break;

            case MODE_HISTORY:
                bindHistoryInfo(holder, storyId);
                break;

            case MODE_FAVORITES:
            default:
                holder.tvRating.setText(
                        String.format(
                                Locale.getDefault(),
                                "★ %.1f · %d đánh giá",
                                story.getRatingAverage(),
                                story.getRatingCount()
                        )
                );
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoryClick(story);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(story);
            }
        });
    }

    private void bindReadingInfo(
            LibraryViewHolder holder,
            String storyId
    ) {
        ReadingProgressEntity progress =
                readingProgressMap.get(storyId);

        if (progress == null) {
            holder.tvMeta.setText("Chưa có thông tin tiến độ");
            holder.tvMeta.setVisibility(View.VISIBLE);
            return;
        }

        int percent = Math.round(progress.getProgressPercent());
        percent = Math.max(0, Math.min(100, percent));

        holder.progressReading.setProgress(percent);
        holder.progressReading.setVisibility(View.VISIBLE);

        String progressText = "Đã đọc " + percent + "%";

        long lastReadAt = progress.getLastReadAt();

        if (lastReadAt > 0) {
            progressText += " · " + formatDate(lastReadAt);
        }

        holder.tvMeta.setText(progressText);
        holder.tvMeta.setVisibility(View.VISIBLE);
    }

    private void bindHistoryInfo(
            LibraryViewHolder holder,
            String storyId
    ) {
        Long readAt = historyReadAtMap.get(storyId);

        if (readAt == null || readAt <= 0) {
            holder.tvMeta.setText("Đã có trong lịch sử đọc");
        } else {
            holder.tvMeta.setText(
                    "Đọc gần nhất: " + formatDate(readAt)
            );
        }

        holder.tvMeta.setVisibility(View.VISIBLE);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        );

        return dateFormat.format(new Date(timestamp));
    }

    private void loadCover(ImageView imageView, String imageUrl) {
        imageView.setImageResource(PLACEHOLDER);
        imageView.setTag(imageUrl);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        Bitmap cachedBitmap = imageCache.get(imageUrl);

        if (cachedBitmap != null && !cachedBitmap.isRecycled()) {
            imageView.setImageBitmap(cachedBitmap);
            return;
        }

        imageExecutor.execute(() -> {
            Bitmap bitmap = downloadImage(imageUrl);

            if (bitmap == null) {
                return;
            }

            imageCache.put(imageUrl, bitmap);

            imageView.post(() -> {
                Object currentTag = imageView.getTag();

                if (imageUrl.equals(currentTag)) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private Bitmap downloadImage(String imageUrl) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(imageUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "User-Agent",
                    "StoryVerse Android App"
            );

            if (connection.getResponseCode()
                    != HttpURLConnection.HTTP_OK) {
                return null;
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(inputStream);
            }

        } catch (Exception e) {
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    static class LibraryViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivCover;
        final TextView tvTitle;
        final TextView tvDescription;
        final TextView tvChapters;
        final TextView tvRating;
        final TextView tvMeta;
        final ProgressBar progressReading;
        final ImageButton btnRemove;

        LibraryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCover = itemView.findViewById(R.id.ivItemCover);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvChapters = itemView.findViewById(R.id.tvItemChapters);
            tvRating = itemView.findViewById(R.id.tvItemRating);
            tvMeta = itemView.findViewById(R.id.tvItemMeta);
            progressReading = itemView.findViewById(
                    R.id.progressItemReading
            );
            btnRemove = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
