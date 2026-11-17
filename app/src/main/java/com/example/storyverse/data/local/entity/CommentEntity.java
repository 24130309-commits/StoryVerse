
package com.example.storyverse.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "comments",
        indices = {
                @Index(value = {"userId"}),
                @Index(value = {"storyId"}),
                @Index(value = {"chapterId"}),
                @Index(value = {"parentCommentId"})
        }
)
public class CommentEntity {

    @PrimaryKey
    @NonNull
    private String commentId;

    private String userId;
    private String storyId;
    private String chapterId;

    private String content;

    // Null nếu đây là bình luận gốc
    private String parentCommentId;

    private long likeCount;

    private String status;

    private long createdAt;
    private long updatedAt;

    // Constructor dành cho Room
    public CommentEntity() {
    }

    // Constructor sử dụng trong Java
    @Ignore
    public CommentEntity(
            @NonNull String commentId,
            String userId,
            String storyId,
            String chapterId,
            String content,
            String parentCommentId,
            long likeCount,
            String status,
            long createdAt,
            long updatedAt
    ) {
        this.commentId = commentId;
        this.userId = userId;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.likeCount = likeCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(@NonNull String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
