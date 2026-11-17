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

    // Thêm một lịch sử đọc
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(ReadingHistoryEntity history);

    // Thêm nhiều lịch sử đọc
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistories(List<ReadingHistoryEntity> histories);

    // Lấy lịch sử đọc theo người dùng
    @Query("SELECT * FROM reading_history " +
            "WHERE userId = :userId ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByUserId(String userId);

    // Giữ phương thức cũ để tương thích
    @Query("SELECT * FROM reading_history " +
            "WHERE userId = :userId ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByUser(String userId);

    // Lấy lịch sử đọc của một truyện theo người dùng
    @Query("SELECT * FROM reading_history " +
            "WHERE userId = :userId AND storyId = :storyId " +
            "ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByStoryId(
            String userId,
            String storyId
    );

    // Giữ phương thức cũ để tương thích
    @Query("SELECT * FROM reading_history " +
            "WHERE userId = :userId AND storyId = :storyId " +
            "ORDER BY readAt DESC")
    List<ReadingHistoryEntity> getHistoryByStory(
            String userId,
            String storyId
    );

    // Lấy lịch sử đọc gần đây
    @Query("SELECT * FROM reading_history " +
            "WHERE userId = :userId " +
            "ORDER BY readAt DESC LIMIT :limit")
    List<ReadingHistoryEntity> getRecentHistory(
            String userId,
            int limit
    );

    // Đếm lịch sử đọc của người dùng
    @Query("SELECT COUNT(*) FROM reading_history WHERE userId = :userId")
    int getHistoryCount(String userId);

    // Xóa một lịch sử đọc theo Entity
    @Delete
    void deleteHistory(ReadingHistoryEntity history);

    // Xóa một lịch sử đọc theo ID
    @Query("DELETE FROM reading_history WHERE historyId = :historyId")
    void deleteHistoryById(String historyId);

    // Xóa lịch sử đọc của người dùng
    @Query("DELETE FROM reading_history WHERE userId = :userId")
    void deleteAllHistoryByUserId(String userId);

    // Giữ phương thức cũ để tương thích
    @Query("DELETE FROM reading_history WHERE userId = :userId")
    void deleteHistoryByUser(String userId);

    // Xóa toàn bộ lịch sử đọc
    @Query("DELETE FROM reading_history")
    void deleteAllHistory();
}