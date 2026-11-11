
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
    // READING PROGRESS
    // =========================

    // Giữ hàm cũ để tương thích với code đang sử dụng.
    public void saveProgress(
            ReadingProgressEntity progress,
            Runnable onComplete
    ) {
        saveProgress(progress, onComplete, null);
    }

    // Lưu tiến độ và nhận thông báo nếu xảy ra lỗi.
    public void saveProgress(
            ReadingProgressEntity progress,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (progress == null) {
                    throw new IllegalArgumentException(
                            "Reading progress không được null"
                    );
                }

                if (!isValidId(progress.getUserId())
                        || !isValidId(progress.getStoryId())
                        || !isValidId(progress.getChapterId())) {

                    throw new IllegalArgumentException(
                            "Thông tin tiến độ đọc không hợp lệ"
                    );
                }

                readingProgressDao.saveProgress(progress);

                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi lưu tiến độ đọc", e);
                postError(onError, e);
            }
        });
    }

    // Lấy tiến độ đọc của một truyện.
    public void getProgress(
            String userId,
            String storyId,
            RepositoryCallback<ReadingProgressEntity> callback
    ) {

        if (!isValidId(userId) || !isValidId(storyId)) {
            postError(
                    callback,
                    new IllegalArgumentException(
                            "User ID hoặc Story ID không hợp lệ"
                    )
            );
            return;
        }

        executorService.execute(() -> {

            try {
                ReadingProgressEntity result =
                        readingProgressDao.getProgress(userId, storyId);

                postSuccess(callback, result);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải tiến độ đọc", e);
                postError(callback, e);
            }
        });
    }

    // Lấy toàn bộ tiến độ đọc của một người dùng.
    public void getProgressByUserId(
            String userId,
            RepositoryCallback<List<ReadingProgressEntity>> callback
    ) {

        if (!isValidId(userId)) {
            postError(
                    callback,
                    new IllegalArgumentException("User ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ReadingProgressEntity> result =
                        readingProgressDao.getProgressByUserId(userId);

                postSuccess(callback, result);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải danh sách tiến độ", e);
                postError(callback, e);
            }
        });
    }

    public void deleteProgress(
            String userId,
            String storyId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {
                readingProgressDao.deleteProgress(userId, storyId);
                postComplete(onComplete);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi xóa tiến độ đọc", e);
            }
        });
    }

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
    // READING HISTORY
    // =========================

    // Giữ hàm cũ để tương thích.
    public void insertHistory(
            ReadingHistoryEntity history,
            Runnable onComplete
    ) {
        insertHistory(history, onComplete, null);
    }

    // Thêm lịch sử đọc và nhận thông báo lỗi.
    public void insertHistory(
            ReadingHistoryEntity history,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (history == null) {
                    throw new IllegalArgumentException(
                            "Reading history không được null"
                    );
                }

                if (!isValidId(history.getUserId())
                        || !isValidId(history.getStoryId())
                        || !isValidId(history.getChapterId())) {

                    throw new IllegalArgumentException(
                            "Thông tin lịch sử đọc không hợp lệ"
                    );
                }

                readingHistoryDao.insertHistory(history);
                postComplete(onComplete);

            } catch (Exception e) {

                Log.e(TAG, "Lỗi khi lưu lịch sử đọc", e);
                postError(onError, e);
            }
        });
    }

    // Lấy lịch sử đọc của người dùng.
    public void getHistoryByUserId(
            String userId,
            RepositoryCallback<List<ReadingHistoryEntity>> callback
    ) {

        if (!isValidId(userId)) {
            postError(
                    callback,
                    new IllegalArgumentException("User ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByUserId(userId);

                postSuccess(callback, result);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải lịch sử đọc", e);
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

        if (!isValidId(userId) || !isValidId(storyId)) {
            postError(
                    callback,
                    new IllegalArgumentException(
                            "User ID hoặc Story ID không hợp lệ"
                    )
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ReadingHistoryEntity> result =
                        readingHistoryDao.getHistoryByStoryId(
                                userId,
                                storyId
                        );

                postSuccess(callback, result);

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi tải lịch sử truyện", e);
                postError(callback, e);
            }
        });
    }

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

    public void deleteAllHistoryByUserId(
            String userId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            try {
                if (!isValidId(userId)) {
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
    // HELPERS
    // =========================

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

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
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private <T> void postError(
            RepositoryCallback<T> callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    private void postError(
            ErrorCallback callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public interface ErrorCallback {
        void onError(Exception e);
    }
}
