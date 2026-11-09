
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.storyverse.data.local.dao.ReadingHistoryDao;
import com.example.storyverse.data.local.dao.ReadingProgressDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadingRepository {

    private static final String TAG = "ReadingRepository";

    private final ReadingProgressDao readingProgressDao;
    private final ReadingHistoryDao readingHistoryDao;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ReadingRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(
                        context.getApplicationContext()
                );

        readingProgressDao = database.readingProgressDao();
        readingHistoryDao = database.readingHistoryDao();
    }

    // =========================
    // Reading Progress
    // =========================

    // Lưu tiến độ đọc.
    public void saveProgress(
            ReadingProgressEntity progress,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                if (progress == null) {
                    throw new IllegalArgumentException(
                            "Reading progress không được null"
                    );
                }

                readingProgressDao.saveProgress(progress);

                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi lưu tiến độ đọc", e);
            }
        });
    }

    // Lấy tiến độ đọc của một truyện.
    public void getProgress(
            String userId,
            String storyId,
            RepositoryCallback<ReadingProgressEntity> callback
    ) {

        executorService.execute(() -> {

            try {

                if (userId == null || userId.isEmpty()
                        || storyId == null || storyId.isEmpty()) {

                    throw new IllegalArgumentException(
                            "User ID hoặc Story ID không hợp lệ"
                    );
                }

                ReadingProgressEntity result =
                        readingProgressDao.getProgress(
                                userId,
                                storyId
                        );

                postSuccess(callback, result);

            } catch (Exception e) {

                postError(callback, e);
            }
        });
    }

    // Lấy toàn bộ tiến độ đọc của một người dùng.
    public void getProgressByUserId(
            String userId,
            RepositoryCallback<List<ReadingProgressEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                if (userId == null || userId.isEmpty()) {
                    throw new IllegalArgumentException(
                            "User ID không hợp lệ"
                    );
                }

                List<ReadingProgressEntity> result =
                        readingProgressDao.getProgressByUserId(userId);

                postSuccess(callback, result);

            } catch (Exception e) {

                postError(callback, e);
            }
        });
    }

    // Xóa tiến độ đọc của một truyện.
    public void deleteProgress(
            String userId,
            String storyId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                readingProgressDao.deleteProgress(
                        userId,
                        storyId
                );

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi xóa tiến độ đọc", e);
            }
        });
    }

    // Xóa toàn bộ tiến độ đọc của một người dùng.
    public void deleteAllProgressByUserId(
            String userId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                readingProgressDao.deleteAllProgressByUserId(userId);

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi xóa toàn bộ tiến độ đọc", e);
            }
        });
    }

    // =========================
    // Reading History
    // =========================

    // Thêm lịch sử đọc.
    public void insertHistory(
            ReadingHistoryEntity history,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                if (history == null) {
                    throw new IllegalArgumentException(
                            "Reading history không được null"
                    );
                }

                readingHistoryDao.insertHistory(history);

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi lưu lịch sử đọc", e);
            }
        });
    }

    // Lấy lịch sử đọc của người dùng.
    public void getHistoryByUserId(
            String userId,
            RepositoryCallback<List<ReadingHistoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                if (userId == null || userId.isEmpty()) {
                    throw new IllegalArgumentException(
                            "User ID không hợp lệ"
                    );
                }

                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByUserId(userId);

                postSuccess(callback, result);

            } catch (Exception e) {

                postError(callback, e);
            }
        });
    }

    // Lấy lịch sử đọc của một truyện.
    public void getHistoryByStoryId(
            String userId,
            String storyId,
            RepositoryCallback<List<ReadingHistoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                if (userId == null || userId.isEmpty()
                        || storyId == null || storyId.isEmpty()) {

                    throw new IllegalArgumentException(
                            "User ID hoặc Story ID không hợp lệ"
                    );
                }

                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByStoryId(
                                userId,
                                storyId
                        );

                postSuccess(callback, result);

            } catch (Exception e) {

                postError(callback, e);
            }
        });
    }

    // Xóa một bản ghi lịch sử đọc.
    public void deleteHistory(
            ReadingHistoryEntity history,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                if (history == null) {
                    throw new IllegalArgumentException(
                            "Reading history không được null"
                    );
                }

                readingHistoryDao.deleteHistory(history);

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi xóa lịch sử đọc", e);
            }
        });
    }

    // Xóa toàn bộ lịch sử đọc của người dùng.
    public void deleteAllHistoryByUserId(
            String userId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {

                if (userId == null || userId.isEmpty()) {
                    throw new IllegalArgumentException(
                            "User ID không hợp lệ"
                    );
                }

                readingHistoryDao.deleteAllHistoryByUserId(userId);

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi xóa toàn bộ lịch sử đọc", e);
            }
        });
    }

    // =========================
    // Callback helpers
    // =========================

    private void postComplete(Runnable callback) {

        if (callback != null) {
            mainHandler.post(callback);
        }
    }

    private <T> void postSuccess(
            RepositoryCallback<T> callback,
            T result
    ) {

        if (callback != null) {
            mainHandler.post(() ->
                    callback.onSuccess(result)
            );
        }
    }

    private <T> void postError(
            RepositoryCallback<T> callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() ->
                    callback.onError(error)
            );
        }
    }

    // =========================
    // Repository Callback
    // =========================

    public interface RepositoryCallback<T> {

        void onSuccess(T result);

        void onError(Exception e);
    }
}
