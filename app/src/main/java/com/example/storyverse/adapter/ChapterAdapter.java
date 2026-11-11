package com.example.storyverse.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChapterAdapter
        extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

    public interface OnChapterClickListener {
        void onChapterClick(ChapterEntity chapter);
    }

    private final List<ChapterEntity> chapters = new ArrayList<>();
    private final OnChapterClickListener listener;

    private String currentChapterId;

    public ChapterAdapter(OnChapterClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chapter, parent, false);

        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChapterViewHolder holder,
            int position
    ) {
        ChapterEntity chapter = chapters.get(position);

        // Hiển thị số chương
        holder.tvChapterNumber.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d",
                        chapter.getChapterNumber()
                )
        );

        // Hiển thị tên chương
        String title = chapter.getTitle();

        holder.tvChapterTitle.setText(
                title != null && !title.trim().isEmpty()
                        ? title
                        : "Chương " + chapter.getChapterNumber()
        );

        // Kiểm tra chương hiện tại
        boolean isCurrent = chapter.getChapterId() != null
                && chapter.getChapterId().equals(currentChapterId);

        View chapterLayout = holder.layoutChapterContent != null
                ? holder.layoutChapterContent
                : holder.itemView;

        if (isCurrent) {
            chapterLayout.setBackgroundResource(
                    R.drawable.bg_chapter_item_selected
            );

            holder.tvChapterNumber.setTextColor(
                    Color.parseColor("#7059D9")
            );

            holder.tvChapterNumber.setBackgroundResource(
                    R.drawable.bg_chapter_item_selected
            );

            holder.tvChapterTitle.setTextColor(
                    Color.parseColor("#7059D9")
            );

            holder.tvChapterStatus.setVisibility(View.VISIBLE);
            holder.ivChapterSelected.setVisibility(View.VISIBLE);

            holder.ivChapterSelected.setColorFilter(
                    Color.parseColor("#7059D9")
            );

        } else {
            chapterLayout.setBackgroundResource(
                    android.R.color.transparent
            );

            holder.tvChapterNumber.setTextColor(
                    Color.parseColor("#555555")
            );

            holder.tvChapterNumber.setBackgroundResource(
                    R.drawable.bg_chapter_number
            );

            holder.tvChapterTitle.setTextColor(
                    Color.parseColor("#222222")
            );

            holder.tvChapterStatus.setVisibility(View.GONE);
            holder.ivChapterSelected.setVisibility(View.GONE);

            holder.ivChapterSelected.clearColorFilter();
        }

        // Xử lý khi nhấn vào một chương
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            ChapterEntity selectedChapter = chapters.get(currentPosition);

            if (listener != null) {
                listener.onChapterClick(selectedChapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    // Cập nhật danh sách chương
    public void setChapters(List<ChapterEntity> newChapters) {
        chapters.clear();

        if (newChapters != null) {
            chapters.addAll(newChapters);
        }

        notifyDataSetChanged();
    }

    // Cập nhật chương đang đọc
    public void setCurrentChapter(String chapterId) {
        String oldChapterId = currentChapterId;

        currentChapterId = chapterId;

        int oldPosition = findChapterPosition(oldChapterId);
        int newPosition = findChapterPosition(currentChapterId);

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }

        if (newPosition != -1 && newPosition != oldPosition) {
            notifyItemChanged(newPosition);
        }
    }

    private int findChapterPosition(String chapterId) {
        if (chapterId == null) {
            return -1;
        }

        for (int i = 0; i < chapters.size(); i++) {
            ChapterEntity chapter = chapters.get(i);

            if (chapterId.equals(chapter.getChapterId())) {
                return i;
            }
        }

        return -1;
    }

    // ViewHolder
    public static class ChapterViewHolder
            extends RecyclerView.ViewHolder {

        View layoutChapterContent;

        TextView tvChapterNumber;
        TextView tvChapterTitle;
        TextView tvChapterStatus;

        ImageView ivChapterSelected;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutChapterContent =
                    itemView.findViewById(R.id.layout_chapter_content);

            tvChapterNumber =
                    itemView.findViewById(R.id.tv_chapter_number);

            tvChapterTitle =
                    itemView.findViewById(R.id.tv_chapter_title);

            tvChapterStatus =
                    itemView.findViewById(R.id.tv_chapter_status);

            ivChapterSelected =
                    itemView.findViewById(R.id.iv_chapter_selected);
        }
    }
}