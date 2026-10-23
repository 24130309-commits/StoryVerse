package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteEntity favorite);

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    List<FavoriteEntity> getFavoritesByUserId(String userId);

    @Query("SELECT * FROM favorites WHERE userId = :userId AND storyId = :storyId LIMIT 1")
    FavoriteEntity getFavorite(String userId, String storyId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND storyId = :storyId)")
    boolean isFavorite(String userId, String storyId);

    @Delete
    void deleteFavorite(FavoriteEntity favorite);

    @Query("DELETE FROM favorites WHERE userId = :userId AND storyId = :storyId")
    void deleteFavorite(String userId, String storyId);

    @Query("DELETE FROM favorites WHERE userId = :userId")
    void deleteAllFavoritesByUserId(String userId);
}