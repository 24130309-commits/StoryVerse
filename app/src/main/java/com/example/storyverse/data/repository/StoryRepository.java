
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.storyverse.data.local.dao.StoryDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.remote.firebase.FirestoreManager;
import com.example.storyverse.data.remote.StoryFirestoreDataSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryRepository {

    private final StoryDao storyDao;
    private final StoryFirestoreDataSource firestoreDataSource;

    private final ExecutorService executorService;
    private final Handler mainHandler;

    public StoryRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context.getApplicationContext());

        storyDao = database.storyDao();

        firestoreDataSource = new StoryFirestoreDataSource();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==============================
    // ROOM - CÁC HÀM CŨ
    // ==============================

    public void insertStory(StoryEntity story) {
        executorService.execute(() -> storyDao.insertStory(story));
    }

    public void insertStories(List<StoryEntity> stories) {
        executorService.execute(() -> storyDao.insertStories(stories));
    }

    public List<StoryEntity> getAllStories() {
        return storyDao.getAllStories();
    }

    public StoryEntity getStoryById(String storyId) {
        return storyDao.getStoryById(storyId);
    }

    public List<StoryEntity> searchStories(String keyword) {
        return storyDao.searchStories(keyword);
    }

    public List<StoryEntity> getStoriesByStatus(String status) {
        return storyDao.getStoriesByStatus(status);
    }

    public void deleteStory(StoryEntity story) {
        executorService.execute(() -> storyDao.deleteStory(story));
    }

    public void deleteAllStories() {
        executorService.execute(storyDao::deleteAllStories);
    }

    // Đếm số lượng truyện hiện đang được lưu trong Room
    public void getCachedStoryCount(
            RepositoryCallback<Integer> callback
    ) {
        executorService.execute(() -> {
            try {
                int count = storyDao.getCachedStoryCount();

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(count);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // ==============================
    // FIRESTORE -> ROOM
    // ==============================

    // Tải danh sách truyện từ Firestore và lưu vào Room
    public void fetchAndCacheStories(
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        firestoreDataSource.getPublishedStories(
                new FirestoreManager.FirestoreCallback<List<StoryEntity>>() {

                    @Override
                    public void onSuccess(List<StoryEntity> stories) {

                        executorService.execute(() -> {
                            try {
                                storyDao.insertStories(stories);

                                mainHandler.post(() -> {
                                    if (callback != null) {
                                        callback.onSuccess(stories);
                                    }
                                });

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
    }

    // Lấy truyện từ Room trước; nếu Room chưa có dữ liệu thì tải Firestore
    public void getStoriesFromCacheOrFirestore(
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        executorService.execute(() -> {

            try {
                List<StoryEntity> cachedStories = storyDao.getAllStories();

                if (cachedStories != null && !cachedStories.isEmpty()) {

                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onSuccess(cachedStories);
                        }
                    });

                } else {
                    fetchAndCacheStories(callback);
                }

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // Lấy một truyện theo ID từ Firestore và cập nhật Room
    public void fetchStoryById(
            String storyId,
            RepositoryCallback<StoryEntity> callback
    ) {
        firestoreDataSource.getStoryById(
                storyId,
                new FirestoreManager.FirestoreCallback<StoryEntity>() {

                    @Override
                    public void onSuccess(StoryEntity story) {

                        if (story == null) {
                            mainHandler.post(() -> {
                                if (callback != null) {
                                    callback.onSuccess(null);
                                }
                            });
                            return;
                        }

                        executorService.execute(() -> {
                            try {
                                storyDao.insertStory(story);

                                mainHandler.post(() -> {
                                    if (callback != null) {
                                        callback.onSuccess(story);
                                    }
                                });

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
    }

    private <T> void postError(
            RepositoryCallback<T> callback,
            Exception e
    ) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onError(e);
            }
        });
    }

    // ==============================
    // CALLBACK
    // ==============================

    public interface RepositoryCallback<T> {
        void onSuccess(T result);

        void onError(Exception e);
    }
}
