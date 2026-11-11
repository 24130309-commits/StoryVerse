
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.storyverse.data.local.dao.ReadingProgressDao;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.data.local.database.StoryVerseDatabase;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadingProgressRepository {

    private final ReadingProgressDao readingProgressDao;

    private final ExecutorService executor;
    private final Handler mainHandler;

    public ReadingProgressRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(
                        context.getApplicationContext()
                );

        readingProgressDao = database.readingProgressDao();

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Lưu tiến độ đọc.
    public void saveProgress(
            ReadingProgressEntity progress,
            RepositoryCallback<ReadingProgressEntity> callback
    ) {

        executor.execute(() -> {

            try {
                validateProgress(progress);

                ReadingProgressEntity existing =
                        readingProgressDao.getProgress(
                                progress.getUserId(),
                                progress.getStoryId()
                        );

                // Giữ nguyên progressId nếu đã có tiến độ trước đó.
                if (existing != null) {
                    progress.setProgressId(existing.getProgressId());
                } else if (progress.getProgressId() == null
                        || progress.getProgressId().trim().isEmpty()) {

                    progress.setProgressId(
                            UUID.randomUUID().toString()
                    );
                }

                if (progress.getLastReadAt() <= 0) {
                    progress.setLastReadAt(
                            System.currentTimeMillis()
                    );
                }

                readingProgressDao.saveProgress(progress);

                notifySuccess(callback, progress);

            } catch (Exception e) {
                notifyError(callback, e);
            }
        });
    }

    // Lấy tiến độ đọc của một truyện theo người dùng.
    public void getProgress(
            String userId,
            String storyId,
            RepositoryCallback<ReadingProgressEntity> callback
    ) {

        executor.execute(() -> {

            try {
                validateIds(userId, storyId);

                ReadingProgressEntity progress =
                        readingProgressDao.getProgress(
                                userId.trim(),
                                storyId.trim()
                        );

                notifySuccess(callback, progress);

            } catch (Exception e) {
                notifyError(callback, e);
            }
        });
    }

    // Lấy toàn bộ tiến độ đọc của một người dùng.
    public void getProgressByUserId(
            String userId,
            RepositoryCallback<List<ReadingProgressEntity>> callback
    ) {

        executor.execute(() -> {

            try {
                if (!isValidId(userId)) {
                    throw new IllegalArgumentException(
                            "User ID không hợp lệ"
                    );
                }

                List<ReadingProgressEntity> progressList =
                        readingProgressDao.getProgressByUserId(
                                userId.trim()
                        );

                notifySuccess(callback, progressList);

            } catch (Exception e) {
                notifyError(callback, e);
            }
        });
    }

    // Xóa tiến độ đọc của một truyện.
    public void deleteProgress(
            String userId,
            String storyId,
            OperationCallback callback
    ) {

        executor.execute(() -> {

            try {
                validateIds(userId, storyId);

                readingProgressDao.deleteProgress(
                        userId.trim(),
                        storyId.trim()
                );

                notifyOperationSuccess(callback);

            } catch (Exception e) {
                notifyOperationError(callback, e);
            }
        });
    }

    // Xóa toàn bộ tiến độ đọc của một người dùng.
    public void deleteAllProgressByUserId(
            String userId,
            OperationCallback callback
    ) {

        executor.execute(() -> {

            try {
                if (!isValidId(userId)) {
                    throw new IllegalArgumentException(
                            "User ID không hợp lệ"
                    );
                }

                readingProgressDao.deleteAllProgressByUserId(
                        userId.trim()
                );

                notifyOperationSuccess(callback);

            } catch (Exception e) {
                notifyOperationError(callback, e);
            }
        });
    }

    // Kiểm tra dữ liệu trước khi lưu.
    private void validateProgress(
            ReadingProgressEntity progress
    ) {

        if (progress == null) {
            throw new IllegalArgumentException(
                    "Reading progress không được null"
            );
        }

        validateIds(
                progress.getUserId(),
                progress.getStoryId()
        );

        if (progress.getProgressPercent() < 0
                || progress.getProgressPercent() > 100) {

            throw new IllegalArgumentException(
                    "Tiến độ đọc phải nằm trong khoảng 0 đến 100%"
            );
        }

        if (progress.getLastPosition() < 0) {
            throw new IllegalArgumentException(
                    "Vị trí đọc không được âm"
            );
        }
    }

    private void validateIds(
            String userId,
            String storyId
    ) {

        if (!isValidId(userId) || !isValidId(storyId)) {
            throw new IllegalArgumentException(
                    "User ID hoặc Story ID không hợp lệ"
            );
        }
    }

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    // Trả kết quả thành công về Main Thread.
    private <T> void notifySuccess(
            RepositoryCallback<T> callback,
            T result
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    // Trả lỗi về Main Thread.
    private void notifyError(
            RepositoryCallback<?> callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    private void notifyOperationSuccess(
            OperationCallback callback
    ) {

        if (callback != null) {
            mainHandler.post(callback::onSuccess);
        }
    }

    private void notifyOperationError(
            OperationCallback callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    // Callback trả về dữ liệu.
    public interface RepositoryCallback<T> {

        void onSuccess(T result);

        void onError(Exception error);
    }

    // Callback cho thao tác không trả về dữ liệu.
    public interface OperationCallback {

        void onSuccess();

        void onError(Exception error);
    }
}
