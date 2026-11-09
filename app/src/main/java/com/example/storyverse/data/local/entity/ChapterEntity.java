
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "chapters")
public class ChapterEntity {

    @PrimaryKey
    @NonNull
    private String chapterId;

    private String storyId;
    private int chapterNumber;
    private String title;
    private String content;
    private int wordCount;

    private long publishedAt;
    private long createdAt;
    private long updatedAt;

    // Constructor dành cho Room
    public ChapterEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public ChapterEntity(
            @NonNull String chapterId,
            String storyId,
            int chapterNumber,
            String title,
            String content,
            int wordCount,
            long publishedAt,
            long createdAt,
            long updatedAt
    ) {
        this.chapterId = chapterId;
        this.storyId = storyId;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.content = content;
        this.wordCount = wordCount;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(@NonNull String chapterId) {
        this.chapterId = chapterId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
