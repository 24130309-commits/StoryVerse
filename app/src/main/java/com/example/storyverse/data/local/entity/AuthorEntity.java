package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "authors")
public class AuthorEntity {

    @PrimaryKey
    @NonNull
    private String authorId;

    private String name;
    private String avatarUrl;
    private String bio;

    public AuthorEntity() {
    }

    public AuthorEntity(
            @NonNull String authorId,
            String name,
            String avatarUrl,
            String bio
    ) {
        this.authorId = authorId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
    }

    @NonNull
    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(@NonNull String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}