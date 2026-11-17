
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notifications",
        indices = {
                @Index(value = {"userId"}),
                @Index(value = {"createdAt"})
        }
)
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    private String notificationId;

    private String userId;

    private String type;
    private String title;
    private String message;

    // ID của đối tượng liên quan, ví dụ storyId hoặc commentId
    private String referenceId;

    private boolean isRead;

    private long createdAt;

    // Constructor dành cho Room
    public NotificationEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public NotificationEntity(
            @NonNull String notificationId,
            String userId,
            String type,
            String title,
            String message,
            String referenceId,
            boolean isRead,
            long createdAt
    ) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(@NonNull String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
