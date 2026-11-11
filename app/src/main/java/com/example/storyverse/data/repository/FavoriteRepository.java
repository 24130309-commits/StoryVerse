
package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.FavoriteDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.FavoriteEntity;

import java.util.List;

public class FavoriteRepository {

    private final FavoriteDao favoriteDao;

    public FavoriteRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        favoriteDao = database.favoriteDao();
    }

    // =========================================
    // INSERT - THÊM YÊU THÍCH
    // =========================================

    public void insertFavorite(FavoriteEntity favorite) {
        favoriteDao.insertFavorite(favorite);
    }

    // =========================================
    // SELECT - LẤY DANH SÁCH YÊU THÍCH
    // =========================================

    public List<FavoriteEntity> getFavoritesByUserId(String userId) {
        return favoriteDao.getFavoritesByUserId(userId);
    }

    // =========================================
    // SELECT - LẤY YÊU THÍCH THEO NGƯỜI DÙNG VÀ TRUYỆN
    // =========================================

    public FavoriteEntity getFavorite(
            String userId,
            String storyId
    ) {
        return favoriteDao.getFavorite(userId, storyId);
    }

    // =========================================
    // CHECK - KIỂM TRA TRUYỆN ĐÃ YÊU THÍCH
    // =========================================

    public boolean isFavorite(
            String userId,
            String storyId
    ) {
        return favoriteDao.isFavorite(userId, storyId);
    }

    // =========================================
    // DELETE - XÓA YÊU THÍCH
    // =========================================

    public void deleteFavorite(FavoriteEntity favorite) {
        favoriteDao.deleteFavorite(favorite);
    }

    public void deleteFavorite(
            String userId,
            String storyId
    ) {
        favoriteDao.deleteFavoriteByUserAndStory(
                userId,
                storyId
        );
    }

    // =========================================
    // DELETE - XÓA TẤT CẢ YÊU THÍCH CỦA NGƯỜI DÙNG
    // =========================================

    public void deleteAllFavoritesByUserId(String userId) {
        favoriteDao.deleteAllFavoritesByUserId(userId);
    }
}
