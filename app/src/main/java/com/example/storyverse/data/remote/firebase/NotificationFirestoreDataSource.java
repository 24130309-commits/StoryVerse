package com.example.storyverse.data.remote.firebase;

import com.example.storyverse.data.local.entity.NotificationEntity;
import com.example.storyverse.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationFirestoreDataSource {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface Callback {
        void onLoaded(List<NotificationEntity> notifications);
        void onError(Exception error);
    }

    public ListenerRegistration observeNotifications(String userId, Callback callback) {
        return firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error);
                        return;
                    }
                    List<NotificationEntity> notifications = new ArrayList<>();
                    if (snapshots != null) {
                        snapshots.getDocuments().forEach(document -> {
                            NotificationEntity item = new NotificationEntity();
                            item.setNotificationId(document.getId());
                            item.setUserId(document.getString("userId"));
                            item.setType(document.getString("type"));
                            item.setTitle(document.getString("title"));
                            item.setMessage(document.getString("message"));
                            item.setReferenceId(document.getString("referenceId"));
                            Boolean isRead = document.getBoolean("isRead");
                            Long createdAt = document.getLong("createdAt");
                            item.setRead(isRead != null && isRead);
                            item.setCreatedAt(createdAt == null ? 0 : createdAt);
                            notifications.add(item);
                        });
                    }
                    notifications.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    if (callback != null) callback.onLoaded(notifications);
                });
    }
}
