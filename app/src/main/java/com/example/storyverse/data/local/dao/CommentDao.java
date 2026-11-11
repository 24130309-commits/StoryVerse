
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

    // =========================================
    // INSERT - THÊM BÌNH LUẬN
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComment(CommentEntity comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComments(List<CommentEntity> comments);

    // =========================================
    // UPDATE - CẬP NHẬT BÌNH LUẬN
    // =========================================

    @Update
    void updateComment(CommentEntity comment);

    // =========================================
    // DELETE - XÓA BÌNH LUẬN
    // =========================================

    @Delete
    void deleteComment(CommentEntity comment);

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteCommentById(String commentId);

    @Query("DELETE FROM comments WHERE storyId = :storyId")
    void deleteCommentsByStory(String storyId);

    @Query("DELETE FROM comments")
    void deleteAllComments();

    // =========================================
    // SELECT - LẤY BÌNH LUẬN THEO ID
    // =========================================

    @Query("SELECT * FROM comments WHERE commentId = :commentId LIMIT 1")
    CommentEntity getCommentById(String commentId);

    // =========================================
    // SELECT - LẤY BÌNH LUẬN THEO TRUYỆN
    // =========================================

    // Lấy tất cả bình luận theo truyện
    @Query(
            "SELECT * FROM comments WHERE storyId = :storyId " +
                    "ORDER BY createdAt DESC"
    )
    List<CommentEntity> getCommentsByStoryId(String storyId);

    // Lấy bình luận gốc của truyện
    @Query(
            "SELECT * FROM comments WHERE storyId = :storyId " +
                    "AND parentCommentId IS NULL ORDER BY createdAt DESC"
    )
    List<CommentEntity> getRootCommentsByStory(String storyId);

    // =========================================
    // SELECT - LẤY BÌNH LUẬN THEO CHƯƠNG
    // =========================================

    // Lấy tất cả bình luận theo chương
    @Query(
            "SELECT * FROM comments WHERE chapterId = :chapterId " +
                    "ORDER BY createdAt DESC"
    )
    List<CommentEntity> getCommentsByChapterId(String chapterId);

    // Lấy bình luận gốc của chương
    @Query(
            "SELECT * FROM comments WHERE chapterId = :chapterId " +
                    "AND parentCommentId IS NULL ORDER BY createdAt DESC"
    )
    List<CommentEntity> getRootCommentsByChapter(String chapterId);

    // =========================================
    // SELECT - LẤY PHẢN HỒI
    // =========================================

    @Query(
            "SELECT * FROM comments WHERE parentCommentId = :parentCommentId " +
                    "ORDER BY createdAt ASC"
    )
    List<CommentEntity> getReplies(String parentCommentId);

    // =========================================
    // SELECT - LẤY BÌNH LUẬN THEO NGƯỜI DÙNG
    // =========================================

    @Query(
            "SELECT * FROM comments WHERE userId = :userId " +
                    "ORDER BY createdAt DESC"
    )
    List<CommentEntity> getCommentsByUser(String userId);

    // =========================================
    // SELECT - LỌC BÌNH LUẬN THEO TRẠNG THÁI
    // =========================================

    @Query(
            "SELECT * FROM comments WHERE storyId = :storyId " +
                    "AND status = :status ORDER BY createdAt DESC"
    )
    List<CommentEntity> getCommentsByStatus(
            String storyId,
            String status
    );

    // =========================================
    // LIKE - TĂNG / GIẢM LƯỢT THÍCH
    // =========================================

    @Query(
            "UPDATE comments SET likeCount = likeCount + 1 " +
                    "WHERE commentId = :commentId"
    )
    void increaseLikeCount(String commentId);

    @Query(
            "UPDATE comments SET likeCount = CASE " +
                    "WHEN likeCount > 0 THEN likeCount - 1 ELSE 0 END " +
                    "WHERE commentId = :commentId"
    )
    void decreaseLikeCount(String commentId);

    // =========================================
    // UPDATE - SỬA NỘI DUNG BÌNH LUẬN
    // =========================================

    @Query(
            "UPDATE comments SET content = :content, updatedAt = :updatedAt " +
                    "WHERE commentId = :commentId"
    )
    void updateCommentContent(
            String commentId,
            String content,
            long updatedAt
    );
}
