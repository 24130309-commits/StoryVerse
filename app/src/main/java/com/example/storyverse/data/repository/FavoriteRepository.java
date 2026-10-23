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

    public void insertFavorite(FavoriteEntity favorite) {
        favoriteDao.insertFavorite(favorite);
    }

    public List<FavoriteEntity> getFavoritesByUserId(String userId) {
        return favoriteDao.getFavoritesByUserId(userId);
    }

    public FavoriteEntity getFavorite(
            String userId,
            String storyId
    ) {
        return favoriteDao.getFavorite(userId, storyId);
    }

    public boolean isFavorite(
            String userId,
            String storyId
    ) {
        return favoriteDao.isFavorite(userId, storyId);
    }

    public void deleteFavorite(FavoriteEntity favorite) {
        favoriteDao.deleteFavorite(favorite);
    }

    public void deleteFavorite(
            String userId,
            String storyId
    ) {
        favoriteDao.deleteFavorite(userId, storyId);
    }

    public void deleteAllFavoritesByUserId(String userId) {
        favoriteDao.deleteAllFavoritesByUserId(userId);
    }
}