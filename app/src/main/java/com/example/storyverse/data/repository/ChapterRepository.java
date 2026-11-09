
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.storyverse.data.local.dao.ChapterDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.remote.firebase.ChapterFirestoreDataSource;
import com.example.storyverse.data.remote.firebase.FirestoreManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChapterRepository {

    private final ChapterDao chapterDao;
    private final ChapterFirestoreDataSource chapterFirestoreDataSource;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ChapterRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(
                        context.getApplicationContext()
                );

        chapterDao = database.chapterDao();
        chapterFirestoreDataSource = new ChapterFirestoreDataSource();
    }

    // =========================
    // Insert
    // =========================

    public void insertChapter(
            ChapterEntity chapter,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (chapter == null) {
                    throw new IllegalArgumentException(
                            "Chapter không được null"
                    );
                }

                chapterDao.insertChapter(chapter);

                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void insertChapters(
            List<ChapterEntity> chapters,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (chapters == null) {
                    throw new IllegalArgumentException(
                            "Danh sách chapter không được null"
                    );
                }

                chapterDao.insertChapters(chapters);

                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    // =========================
    // Get chapters from Room
    // =========================

    public void getChaptersByStoryId(
            String storyId,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {

        executorService.execute(() -> {

            try {
                List<ChapterEntity> result =
                        chapterDao.getChaptersByStoryId(storyId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterById(
            String chapterId,
            RepositoryCallback<ChapterEntity> callback
    ) {

        executorService.execute(() -> {

            try {
                ChapterEntity result =
                        chapterDao.getChapterById(chapterId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterByNumber(
            String storyId,
            int chapterNumber,
            RepositoryCallback<ChapterEntity> callback
    ) {

        executorService.execute(() -> {

            try {
                ChapterEntity result =
                        chapterDao.getChapterByNumber(
                                storyId,
                                chapterNumber
                        );

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================
    // Cache
    // =========================

    public void isChapterCached(
            String chapterId,
            RepositoryCallback<Boolean> callback
    ) {

        executorService.execute(() -> {

            try {
                boolean result =
                        chapterDao.isChapterCached(chapterId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterCountByStoryId(
            String storyId,
            RepositoryCallback<Integer> callback
    ) {

        executorService.execute(() -> {

            try {
                int result =
                        chapterDao.getChapterCountByStoryId(storyId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================
    // Firestore - Room Sync
    // =========================

    // Tải chương từ Firestore và lưu vào Room.
    public void fetchAndCacheChapters(
            String storyId,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        if (storyId == null || storyId.trim().isEmpty()) {
            postError(
                    onError,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        chapterFirestoreDataSource.getChaptersByStoryId(
                storyId,
                new FirestoreManager.FirestoreCallback<List<ChapterEntity>>() {

                    @Override
                    public void onSuccess(List<ChapterEntity> chapters) {

                        executorService.execute(() -> {

                            try {
                                if (chapters == null) {
                                    throw new IllegalStateException(
                                            "Không nhận được danh sách chương từ Firestore"
                                    );
                                }

                                chapterDao.insertChapters(chapters);
                                postComplete(onComplete);

                            } catch (Exception e) {
                                postError(onError, e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        postError(onError, e);
                    }
                }
        );
    }

    // Ưu tiên lấy chương từ Room.
    // Nếu Room chưa có dữ liệu thì tải từ Firestore và lưu vào Room.
    public void getChaptersFromCacheOrFirestore(
            String storyId,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {

        if (storyId == null || storyId.trim().isEmpty()) {
            postError(
                    callback,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ChapterEntity> cachedChapters =
                        chapterDao.getChaptersByStoryId(storyId);

                if (cachedChapters != null && !cachedChapters.isEmpty()) {
                    postSuccess(callback, cachedChapters);
                    return;
                }

                chapterFirestoreDataSource.getChaptersByStoryId(
                        storyId,
                        new FirestoreManager.FirestoreCallback<List<ChapterEntity>>() {

                            @Override
                            public void onSuccess(List<ChapterEntity> chapters) {

                                executorService.execute(() -> {

                                    try {
                                        if (chapters == null) {
                                            throw new IllegalStateException(
                                                    "Danh sách chương không hợp lệ"
                                            );
                                        }

                                        chapterDao.insertChapters(chapters);
                                        postSuccess(callback, chapters);

                                    } catch (Exception e) {
                                        postError(callback, e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                postError(callback, e);
                            }
                        }
                );

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================
    // Delete
    // =========================

    public void deleteChapter(
            ChapterEntity chapter,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                chapterDao.deleteChapter(chapter);
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void deleteChaptersByStoryId(
            String storyId,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                chapterDao.deleteChaptersByStoryId(storyId);
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void deleteAllChapters(
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                chapterDao.deleteAllChapters();
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
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

    private void postError(
            ErrorCallback callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() ->
                    callback.onError(error)
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

    public interface ErrorCallback {

        void onError(Exception e);
    }
}
