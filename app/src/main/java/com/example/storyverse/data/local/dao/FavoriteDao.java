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

    List<FavoriteEntity> getFavoritesByUserId(String userId);


    boolean isFavorite(String userId, String storyId);

    @Delete
    void deleteFavorite(FavoriteEntity favorite);


    @Query("DELETE FROM favorites WHERE userId = :userId")
    void deleteAllFavoritesByUserId(String userId);
}