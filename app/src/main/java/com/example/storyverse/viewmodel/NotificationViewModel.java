package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.NotificationEntity;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private final MutableLiveData<List<NotificationEntity>> notifications =
            new MutableLiveData<>();

    private final MutableLiveData<Integer> unreadCount =
            new MutableLiveData<>(0);

    public LiveData<List<NotificationEntity>> getNotifications() {
        return notifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    public void setNotifications(
            List<NotificationEntity> notificationList
    ) {
        notifications.setValue(notificationList);
    }

    public void setUnreadCount(int count) {
        unreadCount.setValue(count);
    }
}