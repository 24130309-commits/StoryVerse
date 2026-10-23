package com.example.storyverse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter
        extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<CommentEntity> comments = new ArrayList<>();

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        android.R.layout.simple_list_item_1,
                        parent,
                        false
                );

        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CommentViewHolder holder,
            int position
    ) {
        CommentEntity comment = comments.get(position);

        holder.itemView.setContentDescription(
                comment.getContent()
        );
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<CommentEntity> newComments) {
        comments.clear();

        if (newComments != null) {
            comments.addAll(newComments);
        }

        notifyDataSetChanged();
    }

    public static class CommentViewHolder
            extends RecyclerView.ViewHolder {

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}