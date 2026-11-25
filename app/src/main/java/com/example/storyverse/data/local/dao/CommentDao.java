
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(CommentEntity comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComments(List<CommentEntity> comments);

    @Update
    void updateComment(CommentEntity comment);

    @Delete
    void deleteComment(CommentEntity comment);

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteCommentById(String commentId);

    @Query("DELETE FROM comments WHERE storyId = :storyId")
    void deleteCommentsByStory(String storyId);

    @Query("DELETE FROM comments")
    void deleteAllComments();

    @Query("SELECT * FROM comments WHERE commentId = :commentId LIMIT 1")
    CommentEntity getCommentById(String commentId);

    @Query("SELECT * FROM comments WHERE storyId = :storyId ORDER BY createdAt DESC")
    List<CommentEntity> getCommentsByStoryId(String storyId);

    @Query("SELECT * FROM comments WHERE storyId = :storyId AND parentCommentId IS NULL ORDER BY createdAt DESC")
    List<CommentEntity> getRootCommentsByStory(String storyId);

    @Query("SELECT * FROM comments WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    List<CommentEntity> getCommentsByChapterId(String chapterId);

    @Query("SELECT * FROM comments WHERE chapterId = :chapterId AND parentCommentId IS NULL ORDER BY createdAt DESC")
    List<CommentEntity> getRootCommentsByChapter(String chapterId);

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY createdAt ASC")
    List<CommentEntity> getReplies(String parentCommentId);

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY createdAt DESC")
    List<CommentEntity> getCommentsByUser(String userId);

    @Query("SELECT * FROM comments WHERE storyId = :storyId AND status = :status ORDER BY createdAt DESC")
    List<CommentEntity> getCommentsByStatus(String storyId, String status);

    @Query("UPDATE comments SET likeCount = likeCount + 1 WHERE commentId = :commentId")
    void increaseLikeCount(String commentId);

    @Query("UPDATE comments SET likeCount = CASE WHEN likeCount > 0 THEN likeCount - 1 ELSE 0 END WHERE commentId = :commentId")
    void decreaseLikeCount(String commentId);

    @Query("UPDATE comments SET content = :content, updatedAt = :updatedAt WHERE commentId = :commentId")
    void updateCommentContent(String commentId, String content, long updatedAt);

    @Query("SELECT * FROM comments WHERE parentCommentId IS NULL ORDER BY createdAt DESC")
    List<CommentEntity> getAllRootComments();
}
