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




    @Delete
    void deleteAuthor(AuthorEntity author);

    void deleteAuthorById(String authorId);

    @Query("DELETE FROM authors")
    void deleteAllAuthors();
}