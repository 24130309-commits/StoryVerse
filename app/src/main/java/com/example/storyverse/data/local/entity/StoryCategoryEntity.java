package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "story_categories",
        primaryKeys = {"storyId", "categoryId"}
)
public class StoryCategoryEntity {

    @NonNull
    private String storyId;

    @NonNull
    private String categoryId;

    public StoryCategoryEntity() {
    }

    public StoryCategoryEntity(
            @NonNull String storyId,
            @NonNull String categoryId
    ) {
        this.storyId = storyId;
        this.categoryId = categoryId;
    }

    @NonNull
    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(@NonNull String storyId) {
        this.storyId = storyId;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }
}