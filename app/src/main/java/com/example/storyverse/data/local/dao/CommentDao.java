package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(CommentEntity comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComments(List<CommentEntity> comments);

    @Query("SELECT * FROM comments WHERE storyId = :storyId ORDER BY createdAt DESC")
    List<CommentEntity> getCommentsByStoryId(String storyId);

    @Query("SELECT * FROM comments WHERE chapterId = :chapterId ORDER BY createdAt ASC")
    List<CommentEntity> getCommentsByChapterId(String chapterId);

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY createdAt ASC")
    List<CommentEntity> getReplies(String parentCommentId);

    @Query("SELECT * FROM comments WHERE commentId = :commentId LIMIT 1")
    CommentEntity getCommentById(String commentId);

    @Delete
    void deleteComment(CommentEntity comment);

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteCommentById(String commentId);

    @Query("DELETE FROM comments")
    void deleteAllComments();
}