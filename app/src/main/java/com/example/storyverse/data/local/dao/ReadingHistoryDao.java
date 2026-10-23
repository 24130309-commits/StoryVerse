package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.ReadingHistoryEntity;

import java.util.List;

@Dao
public interface ReadingHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(ReadingHistoryEntity history);

    @Query("SELECT * FROM reading_history WHERE userId = :userId ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByUserId(String userId);

    @Query("SELECT * FROM reading_history WHERE userId = :userId AND storyId = :storyId ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByStoryId(
            String userId,
            String storyId
    );

    @Delete
    void deleteHistory(ReadingHistoryEntity history);

    @Query("DELETE FROM reading_history WHERE userId = :userId")
    void deleteAllHistoryByUserId(String userId);
}