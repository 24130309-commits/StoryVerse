
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reading_progress",
        indices = {
                @Index(
                        value = {"userId", "storyId"},
                        unique = true
                )
        }
)
public class ReadingProgressEntity {

    @PrimaryKey
    @NonNull
    private String progressId;

    private String userId;
    private String storyId;
    private String chapterId;

    private float progressPercent;
    private int lastPosition;

    private long lastReadAt;

    // Constructor dành cho Room
    public ReadingProgressEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public ReadingProgressEntity(
            @NonNull String progressId,
            String userId,
            String storyId,
            String chapterId,
            float progressPercent,
            int lastPosition,
            long lastReadAt
    ) {
        this.progressId = progressId;
        this.userId = userId;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.progressPercent = progressPercent;
        this.lastPosition = lastPosition;
        this.lastReadAt = lastReadAt;
    }

    @NonNull
    public String getProgressId() {
        return progressId;
    }

    public void setProgressId(@NonNull String progressId) {
        this.progressId = progressId;
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

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public float getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(float progressPercent) {
        this.progressPercent = progressPercent;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public long getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(long lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
