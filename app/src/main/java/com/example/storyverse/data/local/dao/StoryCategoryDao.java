package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.StoryCategoryEntity;

import java.util.List;

@Dao
public interface StoryCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategory(StoryCategoryEntity storyCategory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryCategories(List<StoryCategoryEntity> storyCategories);

    @Query("SELECT * FROM story_categories WHERE storyId = :storyId")
    List<StoryCategoryEntity> getCategoriesByStoryId(String storyId);

    @Query("SELECT * FROM story_categories WHERE categoryId = :categoryId")
    List<StoryCategoryEntity> getStoriesByCategoryId(String categoryId);

    @Query("SELECT EXISTS(" +
            "SELECT 1 FROM story_categories " +
            "WHERE storyId = :storyId AND categoryId = :categoryId)")
    boolean exists(String storyId, String categoryId);

    @Delete
    void deleteStoryCategory(StoryCategoryEntity storyCategory);

    @Query("DELETE FROM story_categories WHERE storyId = :storyId")
    void deleteCategoriesByStoryId(String storyId);

    @Query("DELETE FROM story_categories WHERE categoryId = :categoryId")
    void deleteStoriesByCategoryId(String categoryId);

    @Query("DELETE FROM story_categories")
    void deleteAllStoryCategories();
}