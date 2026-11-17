
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "favorites",
        indices = {
                @Index(
                        value = {"userId", "storyId"},
                        unique = true
                )
        }
)
public class FavoriteEntity {

    @PrimaryKey
    @NonNull
    private String favoriteId;

    private String userId;
    private String storyId;
    private long createdAt;

    // Constructor dành cho Room
    public FavoriteEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public FavoriteEntity(
            @NonNull String favoriteId,
            String userId,
            String storyId,
            long createdAt
    ) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.storyId = storyId;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(@NonNull String favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
