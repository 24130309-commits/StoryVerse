package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotification(NotificationEntity notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotifications(List<NotificationEntity> notifications);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<NotificationEntity> getNotificationsByUserId(String userId);

    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    List<NotificationEntity> getUnreadNotifications(String userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadCount(String userId);

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :notificationId")
    void markAsRead(String notificationId);

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllAsRead(String userId);

    @Delete
    void deleteNotification(NotificationEntity notification);

    @Query("DELETE FROM notifications WHERE userId = :userId")
    void deleteAllNotificationsByUserId(String userId);

    @Query("DELETE FROM notifications")
    void deleteAllNotifications();
}