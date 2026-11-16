package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.ReadingHistoryDao;
import com.example.storyverse.data.local.dao.ReadingProgressDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;

import java.util.List;

public class ReadingRepository {

    private final ReadingProgressDao readingProgressDao;
    private final ReadingHistoryDao readingHistoryDao;

    public ReadingRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        readingProgressDao = database.readingProgressDao();
        readingHistoryDao = database.readingHistoryDao();
    }

    // =========================
    // Reading Progress
    // =========================

    public void saveProgress(ReadingProgressEntity progress) {
        readingProgressDao.saveProgress(progress);
    }

    public ReadingProgressEntity getProgress(
            String userId,
            String storyId
    ) {
        return readingProgressDao.getProgress(
                userId,
                storyId
        );
    }

    public List<ReadingProgressEntity> getProgressByUserId(
            String userId
    ) {
        return readingProgressDao.getProgressByUserId(userId);
    }

    public void deleteProgress(
            String userId,
            String storyId
    ) {
        readingProgressDao.deleteProgress(
                userId,
                storyId
        );
    }

    public void deleteAllProgressByUserId(String userId) {
        readingProgressDao.deleteAllProgressByUserId(userId);
    }

    // =========================
    // Reading History
    // =========================

    public void insertHistory(ReadingHistoryEntity history) {
        readingHistoryDao.insertHistory(history);
    }

    public List<ReadingHistoryEntity> getHistoryByUserId(
            String userId
    ) {
        return readingHistoryDao.getHistoryByUserId(userId);
    }

    public List<ReadingHistoryEntity> getHistoryByStoryId(
            String userId,
            String storyId
    ) {
        return readingHistoryDao.getHistoryByStoryId(
                userId,
                storyId
        );
    }

    public void deleteHistory(ReadingHistoryEntity history) {
        readingHistoryDao.deleteHistory(history);
    }

    public void deleteAllHistoryByUserId(String userId) {
        readingHistoryDao.deleteAllHistoryByUserId(userId);
    }
}