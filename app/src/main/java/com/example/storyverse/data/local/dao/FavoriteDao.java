
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.FavoriteEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    // =========================================
    // INSERT - THÊM YÊU THÍCH
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(FavoriteEntity favorite);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorites(List<FavoriteEntity> favorites);

    // =========================================
    // UPDATE - CẬP NHẬT YÊU THÍCH
    // =========================================

    @Update
    void updateFavorite(FavoriteEntity favorite);

    // =========================================
    // SELECT - LẤY YÊU THÍCH THEO ID
    // =========================================

    @Query("SELECT * FROM favorites WHERE favoriteId = :favoriteId LIMIT 1")
    FavoriteEntity getFavoriteById(String favoriteId);

    // Lấy yêu thích theo người dùng và truyện
    @Query(
            "SELECT * FROM favorites " +
                    "WHERE userId = :userId AND storyId = :storyId " +
                    "LIMIT 1"
    )
    FavoriteEntity getFavorite(String userId, String storyId);

    // =========================================
    // SELECT - LẤY DANH SÁCH YÊU THÍCH
    // =========================================

    @Query(
            "SELECT * FROM favorites " +
                    "WHERE userId = :userId " +
                    "ORDER BY createdAt DESC"
    )
    List<FavoriteEntity> getFavoritesByUserId(String userId);

    // =========================================
    // SELECT - KIỂM TRA YÊU THÍCH
    // =========================================

    @Query(
            "SELECT EXISTS(" +
                    "SELECT 1 FROM favorites " +
                    "WHERE userId = :userId AND storyId = :storyId" +
                    ")"
    )
    boolean isFavorite(String userId, String storyId);

    // =========================================
    // COUNT - ĐẾM SỐ LƯỢNG YÊU THÍCH
    // =========================================

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    int getFavoriteCountByUserId(String userId);

    // =========================================
    // DELETE - XÓA YÊU THÍCH
    // =========================================

    @Delete
    void deleteFavorite(FavoriteEntity favorite);

    @Query(
            "DELETE FROM favorites " +
                    "WHERE userId = :userId AND storyId = :storyId"
    )
    void deleteFavoriteByUserAndStory(String userId, String storyId);

    @Query("DELETE FROM favorites WHERE favoriteId = :favoriteId")
    void deleteFavoriteById(String favoriteId);

    @Query("DELETE FROM favorites WHERE userId = :userId")
    void deleteAllFavoritesByUserId(String userId);

    @Query("DELETE FROM favorites")
    void deleteAllFavorites();
}
