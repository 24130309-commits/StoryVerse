package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stories")
public class StoryEntity {

    @PrimaryKey
    @NonNull
    private String storyId;

    private String title;
    private String description;
    private String coverUrl;
    private String authorId;
    private String status;
    private String language;
    private String ageRating;

    private long viewCount;
    private long favoriteCount;

    private double ratingAverage;
    private long ratingCount;

    private long createdAt;
    private long updatedAt;

    public StoryEntity() {
    }

    public StoryEntity(
            @NonNull String storyId,
            String title,
            String description,
            String coverUrl,
            String authorId,
            String status,
            String language,
            String ageRating,
            long viewCount,
            long favoriteCount,
            double ratingAverage,
            long ratingCount,
            long createdAt,
            long updatedAt
    ) {
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.authorId = authorId;
        this.status = status;
        this.language = language;
        this.ageRating = ageRating;
        this.viewCount = viewCount;
        this.favoriteCount = favoriteCount;
        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(@NonNull String storyId) {
        this.storyId = storyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
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