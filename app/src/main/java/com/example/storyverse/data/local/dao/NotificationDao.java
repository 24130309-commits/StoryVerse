
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    // =========================================
    // INSERT - THÊM THÔNG BÁO
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotifications(List<NotificationEntity> notifications);

    // =========================================
    // UPDATE - CẬP NHẬT THÔNG BÁO
    // =========================================

    @Update
    void updateNotification(NotificationEntity notification);

    // =========================================
    // SELECT - LẤY THÔNG BÁO THEO ID
    // =========================================

    @Query(
            "SELECT * FROM notifications " +
                    "WHERE notificationId = :notificationId LIMIT 1"
    )
    NotificationEntity getNotificationById(String notificationId);

    // =========================================
    // SELECT - LẤY DANH SÁCH THÔNG BÁO
    // =========================================

    @Query(
            "SELECT * FROM notifications " +
                    "WHERE userId = :userId " +
                    "ORDER BY createdAt DESC"
    )
    List<NotificationEntity> getNotificationsByUserId(String userId);

    // Giữ tên cũ để tương thích với code đang sử dụng
    @Query(
            "SELECT * FROM notifications " +
                    "WHERE userId = :userId " +
                    "ORDER BY createdAt DESC"
    )
    List<NotificationEntity> getNotificationsByUser(String userId);

    // =========================================
    // SELECT - LẤY THÔNG BÁO CHƯA ĐỌC
    // =========================================

    @Query(
            "SELECT * FROM notifications " +
                    "WHERE userId = :userId " +
                    "AND isRead = 0 " +
                    "ORDER BY createdAt DESC"
    )
    List<NotificationEntity> getUnreadNotifications(String userId);

    // =========================================
    // COUNT - ĐẾM THÔNG BÁO CHƯA ĐỌC
    // =========================================

    @Query(
            "SELECT COUNT(*) FROM notifications " +
                    "WHERE userId = :userId AND isRead = 0"
    )
    int getUnreadCount(String userId);

    // =========================================
    // UPDATE - ĐÁNH DẤU ĐÃ ĐỌC
    // =========================================

    @Query(
            "UPDATE notifications SET isRead = 1 " +
                    "WHERE notificationId = :notificationId"
    )
    void markAsRead(String notificationId);

    @Query(
            "UPDATE notifications SET isRead = 1 " +
                    "WHERE userId = :userId"
    )
    void markAllAsRead(String userId);

    // =========================================
    // DELETE - XÓA THÔNG BÁO
    // =========================================

    @Delete
    void deleteNotification(NotificationEntity notification);

    @Query(
            "DELETE FROM notifications " +
                    "WHERE notificationId = :notificationId"
    )
    void deleteNotificationById(String notificationId);

    // =========================================
    // DELETE - XÓA THEO NGƯỜI DÙNG
    // =========================================

    @Query(
            "DELETE FROM notifications " +
                    "WHERE userId = :userId"
    )
    void deleteAllNotificationsByUserId(String userId);

    // Giữ tên cũ để tương thích
    @Query(
            "DELETE FROM notifications " +
                    "WHERE userId = :userId"
    )
    void deleteNotificationsByUser(String userId);

    // =========================================
    // DELETE - XÓA TOÀN BỘ
    // =========================================

    @Query("DELETE FROM notifications")
    void deleteAllNotifications();
}
