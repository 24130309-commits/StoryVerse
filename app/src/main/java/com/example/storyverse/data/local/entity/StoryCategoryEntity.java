package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(
        tableName = "story_categories",
        primaryKeys = {"storyId", "categoryId"},

        foreignKeys = {
                @ForeignKey(
                        entity = StoryEntity.class,
                        parentColumns = "storyId",
                        childColumns = "storyId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CategoryEntity.class,
                        parentColumns = "categoryId",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE
                )
        },

        indices = {
                @Index("categoryId")
        }
)
public class StoryCategoryEntity {

    @NonNull
    private String storyId;

    @NonNull
    private String categoryId;

    public StoryCategoryEntity() {
    }

    @Ignore
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