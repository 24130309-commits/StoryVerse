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

    @Query("SELECT * FROM authors ORDER BY name ASC")
    List<AuthorEntity> getAllAuthors();

    @Query("SELECT * FROM authors WHERE authorId = :authorId LIMIT 1")
    AuthorEntity getAuthorById(String authorId);

    @Query("SELECT * FROM authors WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    List<AuthorEntity> searchAuthors(String keyword);

    @Delete
    void deleteAuthor(AuthorEntity author);

    @Query("DELETE FROM authors WHERE authorId = :authorId")
    void deleteAuthorById(String authorId);

    @Query("DELETE FROM authors")
    void deleteAllAuthors();
}