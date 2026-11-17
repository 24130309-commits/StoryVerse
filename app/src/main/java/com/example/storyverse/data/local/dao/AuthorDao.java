package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.AuthorEntity;

import java.util.List;

@Dao
public interface AuthorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthor(AuthorEntity author);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAuthors(List<AuthorEntity> authors);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAuthorIfNotExists(AuthorEntity author);

    @Query("SELECT * FROM authors WHERE authorId = :authorId LIMIT 1")
    AuthorEntity getAuthorById(String authorId);

    @Query("SELECT * FROM authors ORDER BY name COLLATE NOCASE ASC")
    List<AuthorEntity> getAllAuthors();

    @Query("UPDATE authors SET name = CASE WHEN :name IS NOT NULL AND :name != '' THEN :name ELSE name END, avatarUrl = CASE WHEN :avatarUrl IS NOT NULL AND :avatarUrl != '' THEN :avatarUrl ELSE avatarUrl END, bio = CASE WHEN :bio IS NOT NULL AND :bio != '' THEN :bio ELSE bio END WHERE authorId = :authorId")
    void updateAuthorIfNotEmpty(
            String authorId,
            String name,
            String avatarUrl,
            String bio
    );

    @Delete
    void deleteAuthor(AuthorEntity author);

    @Query("DELETE FROM authors WHERE authorId = :authorId")
    void deleteAuthorById(String authorId);

    @Query("DELETE FROM authors")
    void deleteAllAuthors();
}