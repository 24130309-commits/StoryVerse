package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.ReadingHistoryDao;
import com.example.storyverse.data.local.dao.ReadingProgressDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadingRepository {

    private final ReadingProgressDao readingProgressDao;
    private final ReadingHistoryDao readingHistoryDao;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public ReadingRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        readingProgressDao = database.readingProgressDao();
        readingHistoryDao = database.readingHistoryDao();
    }

    // =========================
    // Reading Progress
    // =========================

    public void saveProgress(
            ReadingProgressEntity progress,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingProgressDao.saveProgress(progress);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void getProgress(
            String userId,
            String storyId,
            RepositoryCallback<ReadingProgressEntity> callback
    ) {

        executorService.execute(() -> {

            try {

                ReadingProgressEntity result =
                        readingProgressDao.getProgress(
                                userId,
                                storyId
                        );

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void getProgressByUserId(
            String userId,
            RepositoryCallback<List<ReadingProgressEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<ReadingProgressEntity> result =
                        readingProgressDao.getProgressByUserId(userId);

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void deleteProgress(
            String userId,
            String storyId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingProgressDao.deleteProgress(
                    userId,
                    storyId
            );

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteAllProgressByUserId(
            String userId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingProgressDao.deleteAllProgressByUserId(
                    userId
            );

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // =========================
    // Reading History
    // =========================

    public void insertHistory(
            ReadingHistoryEntity history,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingHistoryDao.insertHistory(history);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void getHistoryByUserId(
            String userId,
            RepositoryCallback<List<ReadingHistoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByUserId(
                                userId
                        );

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void getHistoryByStoryId(
            String userId,
            String storyId,
            RepositoryCallback<List<ReadingHistoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByStoryId(
                                userId,
                                storyId
                        );

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void deleteHistory(
            ReadingHistoryEntity history,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingHistoryDao.deleteHistory(history);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteAllHistoryByUserId(
            String userId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            readingHistoryDao.deleteAllHistoryByUserId(
                    userId
            );

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // =========================
    // Callback
    // =========================

    public interface RepositoryCallback<T> {

        void onSuccess(T result);

        void onError(Exception e);
    }
}