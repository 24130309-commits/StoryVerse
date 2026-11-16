package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.ReadingProgressEntity;

@Dao
public interface ReadingProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(ReadingProgressEntity progress);

    @Query("SELECT * FROM reading_progress WHERE userId = :userId AND storyId = :storyId LIMIT 1")
    ReadingProgressEntity getProgress(String userId, String storyId);

    @Query("SELECT * FROM reading_progress WHERE userId = :userId ORDER BY lastReadAt DESC")
    java.util.List<ReadingProgressEntity> getProgressByUserId(String userId);

    @Query("DELETE FROM reading_progress WHERE userId = :userId AND storyId = :storyId")
    void deleteProgress(String userId, String storyId);

    @Query("DELETE FROM reading_progress WHERE userId = :userId")
    void deleteAllProgressByUserId(String userId);
}