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




    @Delete
    void deleteComment(CommentEntity comment);

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteCommentById(String commentId);

    @Query("DELETE FROM comments")
    void deleteAllComments();
}