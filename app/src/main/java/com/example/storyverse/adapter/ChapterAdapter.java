
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

    // Interface xử lý khi người dùng chọn chương
    public interface OnChapterClickListener {
        void onChapterClick(ChapterEntity chapter);
    }

    private final List<ChapterEntity> chapters = new ArrayList<>();

    private final OnChapterClickListener listener;

    // ID chương hiện tại
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

        // Hiển thị số thứ tự chương
        String chapterNumber = String.format(
                Locale.getDefault(),
                "%02d",
                chapter.getChapterNumber()
        );

        holder.tvChapterNumber.setText(chapterNumber);

        // Hiển thị tên chương
        holder.tvChapterTitle.setText(chapter.getTitle());

        // Kiểm tra chương hiện tại
        boolean isCurrent =
                chapter.getChapterId() != null
                        && chapter.getChapterId().equals(currentChapterId);

        if (isCurrent) {
            // Nền tím nhạt
            holder.layoutChapterContent.setBackgroundResource(
                    R.drawable.bg_chapter_item_selected
            );

            // Màu số chương
            holder.tvChapterNumber.setTextColor(
                    Color.parseColor("#7059D9")
            );

            // Nền ô số chương
            holder.tvChapterNumber.setBackgroundResource(
                    R.drawable.bg_chapter_item_selected
            );

            // Màu tên chương
            holder.tvChapterTitle.setTextColor(
                    Color.parseColor("#7059D9")
            );

            // Hiện nhãn chương đang đọc
            holder.tvChapterStatus.setVisibility(View.VISIBLE);

            // Hiện dấu tích
            holder.ivChapterSelected.setVisibility(View.VISIBLE);

            holder.ivChapterSelected.setColorFilter(
                    Color.parseColor("#7059D9")
            );

        } else {
            // Giao diện chương bình thường
            holder.layoutChapterContent.setBackgroundResource(
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

            // Ẩn nhãn và dấu tích
            holder.tvChapterStatus.setVisibility(View.GONE);
            holder.ivChapterSelected.setVisibility(View.GONE);
        }

        // Xử lý khi nhấn chọn chương
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChapterClick(chapter);
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
        currentChapterId = chapterId;
        notifyDataSetChanged();
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
