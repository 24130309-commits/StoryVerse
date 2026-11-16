package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "reading_history",
        indices = {
                @androidx.room.Index(value = {"userId"}),
                @androidx.room.Index(value = {"storyId"}),
                @androidx.room.Index(value = {"readAt"})
        }
)
public class ReadingHistoryEntity {

    @PrimaryKey
    @NonNull
    private String historyId;

    private String userId;
    private String storyId;
    private String chapterId;

    private long readAt;

    public ReadingHistoryEntity() {
    }

    public ReadingHistoryEntity(
            @NonNull String historyId,
            String userId,
            String storyId,
            String chapterId,
            long readAt
    ) {
        this.historyId = historyId;
        this.userId = userId;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.readAt = readAt;
    }

    @NonNull
    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(@NonNull String historyId) {
        this.historyId = historyId;
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

    public long getReadAt() {
        return readAt;
    }

    public void setReadAt(long readAt) {
        this.readAt = readAt;
    }
}