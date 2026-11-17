
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey
    @NonNull
    private String categoryId;

    private String name;
    private String description;
    private String iconUrl;

    // Constructor dành cho Room
    public CategoryEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public CategoryEntity(
            @NonNull String categoryId,
            String name,
            String description,
            String iconUrl
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
