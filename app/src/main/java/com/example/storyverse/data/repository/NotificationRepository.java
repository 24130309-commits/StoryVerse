package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.NotificationDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.NotificationEntity;

import java.util.List;

public class NotificationRepository {

    private final NotificationDao notificationDao;

    public NotificationRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        notificationDao = database.notificationDao();
    }

    public void insertNotification(
            NotificationEntity notification
    ) {
        notificationDao.insertNotification(notification);
    }

    public void insertNotifications(
            List<NotificationEntity> notifications
    ) {
        notificationDao.insertNotifications(notifications);
    }

    public List<NotificationEntity> getNotificationsByUserId(
            String userId
    ) {
        return notificationDao.getNotificationsByUserId(userId);
    }

    public List<NotificationEntity> getUnreadNotifications(
            String userId
    ) {
        return notificationDao.getUnreadNotifications(userId);
    }

    public int getUnreadCount(String userId) {
        return notificationDao.getUnreadCount(userId);
    }

    public void markAsRead(String notificationId) {
        notificationDao.markAsRead(notificationId);
    }

    public void markAllAsRead(String userId) {
        notificationDao.markAllAsRead(userId);
    }

    public void deleteNotification(
            NotificationEntity notification
    ) {
        notificationDao.deleteNotification(notification);
    }

    public void deleteAllNotificationsByUserId(
            String userId
    ) {
        notificationDao.deleteAllNotificationsByUserId(userId);
    }

    public void deleteAllNotifications() {
        notificationDao.deleteAllNotifications();
    }
}