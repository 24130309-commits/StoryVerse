
package com.example.storyverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.FavoriteRepository;
import com.example.storyverse.data.repository.ReadingRepository;
import com.example.storyverse.data.repository.StoryRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryViewModel extends AndroidViewModel {

    private static final int LOAD_FAVORITES = 1;
    private static final int LOAD_READING = 2;
    private static final int LOAD_HISTORY = 3;
    private static final int LOAD_ALL = 4;

    private final FavoriteRepository favoriteRepository;
    private final ReadingRepository readingRepository;
    private final StoryRepository storyRepository;

    private final ExecutorService executorService;

    private final MutableLiveData<List<StoryEntity>> favoriteStories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<StoryEntity>> readingStories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<StoryEntity>> historyStories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Map<String, ReadingProgressEntity>>
            readingProgressMap = new MutableLiveData<>(new LinkedHashMap<>());

    private final MutableLiveData<Map<String, Long>>
            historyReadAtMap = new MutableLiveData<>(new LinkedHashMap<>());

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    private String lastUserId;
    private int lastLoadType = LOAD_ALL;

    public LibraryViewModel(@NonNull Application application) {
        super(application);

        favoriteRepository = new FavoriteRepository(application);
        readingRepository = new ReadingRepository(application);
        storyRepository = new StoryRepository(application);

        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<StoryEntity>> getFavoriteStories() {
        return favoriteStories;
    }

    public LiveData<List<StoryEntity>> getReadingStories() {
        return readingStories;
    }

    public LiveData<List<StoryEntity>> getHistoryStories() {
        return historyStories;
    }

    public LiveData<Map<String, ReadingProgressEntity>>
    getReadingProgressMap() {
        return readingProgressMap;
    }

    public LiveData<Map<String, Long>> getHistoryReadAtMap() {
        return historyReadAtMap;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadLibrary(String userId) {
        if (!isValidUserId(userId)) {
            errorMessage.setValue(
                    "Không xác định được tài khoản người dùng."
            );
            return;
        }

        lastUserId = userId;
        lastLoadType = LOAD_ALL;

        executorService.execute(() -> {
            loading.postValue(true);
            errorMessage.postValue(null);

            try {
                migrateGuestFavorites(userId);

                List<StoryEntity> favorites =
                        getFavoriteStoriesFromDatabase(userId);

                List<ReadingProgressEntity> progresses =
                        readingRepository.getProgressByUserId(userId);

                List<StoryEntity> reading =
                        buildReadingStories(progresses);

                Map<String, ReadingProgressEntity> progressMap =
                        buildProgressMap(progresses);

                List<ReadingHistoryEntity> histories =
                        readingRepository.getHistoryByUserId(userId);

                List<StoryEntity> history =
                        buildHistoryStories(histories);

                Map<String, Long> readAtMap =
                        buildHistoryReadAtMap(histories);

                favoriteStories.postValue(favorites);
                readingStories.postValue(reading);
                historyStories.postValue(history);

                readingProgressMap.postValue(progressMap);
                historyReadAtMap.postValue(readAtMap);

            } catch (Exception e) {
                errorMessage.postValue(
                        getErrorMessage(e, "Không thể tải thư viện.")
                );
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void loadFavorites(String userId) {
        if (!isValidUserId(userId)) {
            errorMessage.setValue(
                    "Không xác định được tài khoản người dùng."
            );
            return;
        }

        lastUserId = userId;
        lastLoadType = LOAD_FAVORITES;

        executorService.execute(() -> {
            loading.postValue(true);
            errorMessage.postValue(null);

            try {
                migrateGuestFavorites(userId);

                List<StoryEntity> stories =
                        getFavoriteStoriesFromDatabase(userId);

                favoriteStories.postValue(stories);

            } catch (Exception e) {
                errorMessage.postValue(
                        getErrorMessage(
                                e,
                                "Không thể tải danh sách yêu thích."
                        )
                );
            } finally {
                loading.postValue(false);
            }
        });
    }

    private List<StoryEntity> getFavoriteStoriesFromDatabase(
            String userId
    ) {
        List<FavoriteEntity> favorites =
                favoriteRepository.getFavoritesByUserId(userId);

        List<StoryEntity> stories = new ArrayList<>();

        if (favorites == null) {
            return stories;
        }

        for (FavoriteEntity favorite : favorites) {
            if (favorite == null
                    || favorite.getStoryId() == null
                    || favorite.getStoryId().trim().isEmpty()) {
                continue;
            }

            StoryEntity story = storyRepository.getStoryById(
                    favorite.getStoryId()
            );

            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }

    public void removeFavorite(String userId, String storyId) {
        if (!isValidUserId(userId)
                || storyId == null
                || storyId.trim().isEmpty()) {
            errorMessage.setValue("Thông tin truyện không hợp lệ.");
            return;
        }

        lastUserId = userId;
        lastLoadType = LOAD_FAVORITES;

        executorService.execute(() -> {
            loading.postValue(true);
            errorMessage.postValue(null);

            try {
                favoriteRepository.deleteFavorite(userId, storyId);

                List<StoryEntity> stories =
                        getFavoriteStoriesFromDatabase(userId);

                favoriteStories.postValue(stories);

            } catch (Exception e) {
                errorMessage.postValue(
                        getErrorMessage(
                                e,
                                "Không thể xóa truyện khỏi thư viện."
                        )
                );
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void loadReadingStories(String userId) {
        if (!isValidUserId(userId)) {
            errorMessage.setValue(
                    "Không xác định được tài khoản người dùng."
            );
            return;
        }

        lastUserId = userId;
        lastLoadType = LOAD_READING;

        executorService.execute(() -> {
            loading.postValue(true);
            errorMessage.postValue(null);

            try {
                List<ReadingProgressEntity> progresses =
                        readingRepository.getProgressByUserId(userId);

                List<StoryEntity> stories =
                        buildReadingStories(progresses);

                Map<String, ReadingProgressEntity> progressMap =
                        buildProgressMap(progresses);

                readingStories.postValue(stories);
                readingProgressMap.postValue(progressMap);

            } catch (Exception e) {
                errorMessage.postValue(
                        getErrorMessage(
                                e,
                                "Không thể tải danh sách đang đọc."
                        )
                );
            } finally {
                loading.postValue(false);
            }
        });
    }

    private List<StoryEntity> buildReadingStories(
            List<ReadingProgressEntity> progresses
    ) {
        List<StoryEntity> stories = new ArrayList<>();

        if (progresses == null) {
            return stories;
        }

        for (ReadingProgressEntity progress : progresses) {
            if (progress == null
                    || progress.getStoryId() == null
                    || progress.getStoryId().trim().isEmpty()) {
                continue;
            }

            StoryEntity story = storyRepository.getStoryById(
                    progress.getStoryId()
            );

            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }

    private Map<String, ReadingProgressEntity> buildProgressMap(
            List<ReadingProgressEntity> progresses
    ) {
        Map<String, ReadingProgressEntity> result =
                new LinkedHashMap<>();

        if (progresses == null) {
            return result;
        }

        for (ReadingProgressEntity progress : progresses) {
            if (progress == null
                    || progress.getStoryId() == null
                    || progress.getStoryId().trim().isEmpty()) {
                continue;
            }

            result.put(progress.getStoryId(), progress);
        }

        return result;
    }

    public void loadHistoryStories(String userId) {
        if (!isValidUserId(userId)) {
            errorMessage.setValue(
                    "Không xác định được tài khoản người dùng."
            );
            return;
        }

        lastUserId = userId;
        lastLoadType = LOAD_HISTORY;

        executorService.execute(() -> {
            loading.postValue(true);
            errorMessage.postValue(null);

            try {
                List<ReadingHistoryEntity> histories =
                        readingRepository.getHistoryByUserId(userId);

                List<StoryEntity> stories =
                        buildHistoryStories(histories);

                Map<String, Long> readAtMap =
                        buildHistoryReadAtMap(histories);

                historyStories.postValue(stories);
                historyReadAtMap.postValue(readAtMap);

            } catch (Exception e) {
                errorMessage.postValue(
                        getErrorMessage(
                                e,
                                "Không thể tải lịch sử đọc."
                        )
                );
            } finally {
                loading.postValue(false);
            }
        });
    }

    private List<StoryEntity> buildHistoryStories(
            List<ReadingHistoryEntity> histories
    ) {
        Map<String, StoryEntity> uniqueStories =
                new LinkedHashMap<>();

        if (histories == null) {
            return new ArrayList<>();
        }

        for (ReadingHistoryEntity history : histories) {
            if (history == null
                    || history.getStoryId() == null
                    || history.getStoryId().trim().isEmpty()) {
                continue;
            }

            String storyId = history.getStoryId();

            if (uniqueStories.containsKey(storyId)) {
                continue;
            }

            StoryEntity story = storyRepository.getStoryById(storyId);

            if (story != null) {
                uniqueStories.put(storyId, story);
            }
        }

        return new ArrayList<>(uniqueStories.values());
    }

    private Map<String, Long> buildHistoryReadAtMap(
            List<ReadingHistoryEntity> histories
    ) {
        Map<String, Long> result = new LinkedHashMap<>();

        if (histories == null) {
            return result;
        }

        for (ReadingHistoryEntity history : histories) {
            if (history == null
                    || history.getStoryId() == null
                    || history.getStoryId().trim().isEmpty()) {
                continue;
            }

            String storyId = history.getStoryId();

            if (!result.containsKey(storyId)) {
                result.put(storyId, history.getReadAt());
            }
        }

        return result;
    }

    public void retryLastLoad() {
        if (!isValidUserId(lastUserId)) {
            return;
        }

        switch (lastLoadType) {
            case LOAD_FAVORITES:
                loadFavorites(lastUserId);
                break;

            case LOAD_READING:
                loadReadingStories(lastUserId);
                break;

            case LOAD_HISTORY:
                loadHistoryStories(lastUserId);
                break;

            default:
                loadLibrary(lastUserId);
                break;
        }
    }

    private void migrateGuestFavorites(String userId) {
        if (userId.startsWith("guest_")) {
            favoriteRepository.migrateGuestFavorites(
                    "guest_user",
                    userId
            );
        }
    }

    private boolean isValidUserId(String userId) {
        return userId != null && !userId.trim().isEmpty();
    }

    private String getErrorMessage(
            Exception exception,
            String defaultMessage
    ) {
        if (exception != null
                && exception.getMessage() != null
                && !exception.getMessage().trim().isEmpty()) {
            return exception.getMessage();
        }

        return defaultMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
