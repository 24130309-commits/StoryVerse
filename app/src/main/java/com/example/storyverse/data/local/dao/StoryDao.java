package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

@Dao
public interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStory(StoryEntity story);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStories(List<StoryEntity> stories);

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    List<StoryEntity> getAllStories();

    @Query("SELECT * FROM stories WHERE storyId = :storyId LIMIT 1")
    StoryEntity getStoryById(String storyId);

    @Query("SELECT * FROM stories WHERE title LIKE '%' || :keyword || '%' ORDER BY title ASC")
    List<StoryEntity> searchStories(String keyword);

    @Query("SELECT * FROM stories WHERE status = :status ORDER BY updatedAt DESC")
    List<StoryEntity> getStoriesByStatus(String status);

    @Delete
    void deleteStory(StoryEntity story);

    @Query("DELETE FROM stories")
    void deleteAllStories();
}