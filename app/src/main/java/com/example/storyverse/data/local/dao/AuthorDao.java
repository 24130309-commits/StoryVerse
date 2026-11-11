package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.AuthorEntity;

import java.util.List;

@Dao
public interface AuthorDao {

    // =========================================
    // INSERT - THÊM TÁC GIẢ
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthor(AuthorEntity author);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthors(List<AuthorEntity> authors);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAuthorIfNotExists(AuthorEntity author);

    // =========================================
    // UPDATE - CẬP NHẬT TÁC GIẢ
    // =========================================

    @Update
    void updateAuthor(AuthorEntity author);

    // Chỉ cập nhật trường có dữ liệu hợp lệ
    @Query(
            "UPDATE authors SET " +
                    "name = CASE " +
                    "WHEN :name IS NOT NULL AND TRIM(:name) != '' " +
                    "THEN :name ELSE name END, " +
                    "avatarUrl = CASE " +
                    "WHEN :avatarUrl IS NOT NULL AND TRIM(:avatarUrl) != '' " +
                    "THEN :avatarUrl ELSE avatarUrl END, " +
                    "bio = CASE " +
                    "WHEN :bio IS NOT NULL AND TRIM(:bio) != '' " +
                    "THEN :bio ELSE bio END " +
                    "WHERE authorId = :authorId"
    )
    void updateAuthorIfNotEmpty(
            String authorId,
            String name,
            String avatarUrl,
            String bio
    );

    // =========================================
    // DELETE - XÓA TÁC GIẢ
    // =========================================

    @Delete
    void deleteAuthor(AuthorEntity author);

    @Query(
            "DELETE FROM authors " +
                    "WHERE authorId = :authorId"
    )
    void deleteAuthorById(String authorId);

    @Query("DELETE FROM authors")
    void deleteAllAuthors();

    // =========================================
    // SELECT - LẤY TÁC GIẢ
    // =========================================

    @Query(
            "SELECT * FROM authors " +
                    "WHERE authorId = :authorId LIMIT 1"
    )
    AuthorEntity getAuthorById(String authorId);

    @Query(
            "SELECT * FROM authors " +
                    "ORDER BY name COLLATE NOCASE ASC"
    )
    List<AuthorEntity> getAllAuthors();

    // =========================================
    // SEARCH - TÌM KIẾM TÁC GIẢ
    // =========================================

    @Query(
            "SELECT * FROM authors " +
                    "WHERE name LIKE '%' || :keyword || '%' " +
                    "ORDER BY name ASC"
    )
    List<AuthorEntity> searchAuthors(String keyword);

    // =========================================
    // COUNT - ĐẾM TÁC GIẢ
    // =========================================

    @Query("SELECT COUNT(*) FROM authors")
    int getAuthorCount();
}