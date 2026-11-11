package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.ReadingProgressEntity;

import java.util.List;

@Dao
public interface ReadingProgressDao {

    // Lưu hoặc cập nhật tiến độ đọc
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(ReadingProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgress(ReadingProgressEntity progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProgressList(List<ReadingProgressEntity> progressList);

    @Update
    void updateProgress(ReadingProgressEntity progress);

    // Lấy tiến độ đọc theo người dùng và truyện
    @Query("SELECT * FROM reading_progress " +
            "WHERE userId = :userId AND storyId = :storyId LIMIT 1")
    ReadingProgressEntity getProgress(String userId, String storyId);

    // Lấy toàn bộ tiến độ đọc của người dùng
    @Query("SELECT * FROM reading_progress " +
            "WHERE userId = :userId ORDER BY lastReadAt DESC")
    List<ReadingProgressEntity> getProgressByUserId(String userId);

    // Giữ phương thức cũ để tương thích
    @Query("SELECT * FROM reading_progress " +
            "WHERE userId = :userId ORDER BY lastReadAt DESC")
    List<ReadingProgressEntity> getProgressByUser(String userId);

    // Cập nhật vị trí đọc
    @Query("UPDATE reading_progress SET chapterId = :chapterId, " +
            "progressPercent = :progressPercent, " +
            "lastPosition = :lastPosition, " +
            "lastReadAt = :lastReadAt " +
            "WHERE userId = :userId AND storyId = :storyId")
    void updateReadingPosition(
            String userId,
            String storyId,
            String chapterId,
            float progressPercent,
            int lastPosition,
            long lastReadAt
    );

    // Xóa tiến độ đọc của một truyện
    @Query("DELETE FROM reading_progress " +
            "WHERE userId = :userId AND storyId = :storyId")
    void deleteProgress(String userId, String storyId);

    // Xóa tiến độ đọc theo người dùng (tên cũ)
    @Query("DELETE FROM reading_progress WHERE userId = :userId")
    void deleteProgressByUser(String userId);

    // Xóa toàn bộ tiến độ đọc của một người dùng
    @Query("DELETE FROM reading_progress WHERE userId = :userId")
    void deleteAllProgressByUserId(String userId);

    // Xóa toàn bộ tiến độ đọc trong cơ sở dữ liệu
    @Query("DELETE FROM reading_progress")
    void deleteAllProgress();
}