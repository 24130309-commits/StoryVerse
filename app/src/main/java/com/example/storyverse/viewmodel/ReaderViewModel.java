
package com.example.storyverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.data.repository.ChapterRepository;
import com.example.storyverse.data.repository.ReadingRepository;

import java.util.ArrayList;
import java.util.List;

public class ReaderViewModel extends AndroidViewModel {

    private final ChapterRepository chapterRepository;
    private final ReadingRepository readingRepository;

    private final MutableLiveData<List<ChapterEntity>> chapters =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<ChapterEntity> currentChapter =
            new MutableLiveData<>();

    private final MutableLiveData<ReadingProgressEntity> readingProgress =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    // Trạng thái riêng để phân biệt:
    // Chưa tải xong tiến độ và đã tải xong nhưng không có tiến độ.
    private final MutableLiveData<Boolean> progressLoading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private String currentStoryId;

    private String currentProgressUserId;

    public ReaderViewModel(@NonNull Application application) {
        super(application);

        chapterRepository = new ChapterRepository(application);
        readingRepository = new ReadingRepository(application);
    }

    // ==========================================
    // LIVE DATA
    // ==========================================

    public LiveData<List<ChapterEntity>> getChapters() {
        return chapters;
    }

    public LiveData<ChapterEntity> getCurrentChapter() {
        return currentChapter;
    }

    public LiveData<ReadingProgressEntity> getReadingProgress() {
        return readingProgress;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getProgressLoading() {
        return progressLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    // ==========================================
    // LOAD CHAPTERS
    // ==========================================

    public void loadChapters(String storyId) {

        if (storyId == null || storyId.trim().isEmpty()) {
            error.setValue("Story ID không hợp lệ");
            return;
        }

        currentStoryId = storyId;
        currentProgressUserId = null;

        chapters.setValue(new ArrayList<>());
        currentChapter.setValue(null);
        readingProgress.setValue(null);

        loading.setValue(true);
        progressLoading.setValue(false);
        error.setValue(null);

        chapterRepository.getChaptersFromCacheOrFirestore(
                storyId,
                new ChapterRepository.RepositoryCallback<List<ChapterEntity>>() {

                    @Override
                    public void onSuccess(List<ChapterEntity> result) {

                        if (!storyId.equals(currentStoryId)) {
                            return;
                        }

                        List<ChapterEntity> safeResult =
                                result != null
                                        ? new ArrayList<>(result)
                                        : new ArrayList<>();

                        chapters.postValue(safeResult);
                        loading.postValue(false);

                        if (safeResult.isEmpty()) {
                            error.postValue(
                                    "Truyện này hiện chưa có chương nào"
                            );
                        } else {
                            error.postValue(null);
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                        if (!storyId.equals(currentStoryId)) {
                            return;
                        }

                        loading.postValue(false);

                        error.postValue(
                                e != null && e.getMessage() != null
                                        ? e.getMessage()
                                        : "Không thể tải danh sách chương"
                        );
                    }
                }
        );
    }

    // ==========================================
    // SELECT CHAPTER
    // ==========================================

    public void selectChapter(ChapterEntity chapter) {

        if (chapter == null) {
            error.setValue("Chương không hợp lệ");
            return;
        }

        currentChapter.setValue(chapter);
        error.setValue(null);
    }

    // ==========================================
    // FIND CHAPTER INDEX
    // ==========================================

    private int findChapterIndex(
            List<ChapterEntity> chapterList,
            String chapterId
    ) {

        if (chapterList == null || chapterId == null) {
            return -1;
        }

        for (int i = 0; i < chapterList.size(); i++) {

            ChapterEntity chapter = chapterList.get(i);

            if (chapter != null
                    && chapterId.equals(chapter.getChapterId())) {

                return i;
            }
        }

        return -1;
    }

    // ==========================================
    // NEXT CHAPTER
    // ==========================================

    public void nextChapter() {

        List<ChapterEntity> chapterList = chapters.getValue();
        ChapterEntity current = currentChapter.getValue();

        if (chapterList == null || chapterList.isEmpty()) {
            return;
        }

        if (current == null) {
            selectChapter(chapterList.get(0));
            return;
        }

        int currentIndex = findChapterIndex(
                chapterList,
                current.getChapterId()
        );

        if (currentIndex < 0) {
            error.setValue("Không tìm thấy chương hiện tại");
            return;
        }

        if (currentIndex < chapterList.size() - 1) {
            selectChapter(chapterList.get(currentIndex + 1));
        } else {
            error.setValue("Bạn đã đọc đến chương cuối");
        }
    }

    // ==========================================
    // PREVIOUS CHAPTER
    // ==========================================

    public void previousChapter() {

        List<ChapterEntity> chapterList = chapters.getValue();
        ChapterEntity current = currentChapter.getValue();

        if (chapterList == null || chapterList.isEmpty()) {
            return;
        }

        if (current == null) {
            selectChapter(chapterList.get(0));
            return;
        }

        int currentIndex = findChapterIndex(
                chapterList,
                current.getChapterId()
        );

        if (currentIndex < 0) {
            error.setValue("Không tìm thấy chương hiện tại");
            return;
        }

        if (currentIndex > 0) {
            selectChapter(chapterList.get(currentIndex - 1));
        } else {
            error.setValue("Đây là chương đầu tiên");
        }
    }

    // ==========================================
    // SAVE READING PROGRESS
    // ==========================================

    public void saveProgress(ReadingProgressEntity progress) {

        if (progress == null) {
            return;
        }

        if (!isValidId(progress.getUserId())
                || !isValidId(progress.getStoryId())
                || !isValidId(progress.getChapterId())) {

            error.setValue("Thông tin tiến độ đọc không hợp lệ");
            return;
        }

        readingRepository.saveProgress(
                progress,
                () -> readingProgress.setValue(progress),
                e -> error.setValue(
                        e != null && e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể lưu tiến độ đọc"
                )
        );
    }

    // ==========================================
    // LOAD READING PROGRESS
    // ==========================================

    public void loadProgress(String userId, String storyId) {

        if (!isValidId(userId) || !isValidId(storyId)) {

            progressLoading.setValue(false);

            error.setValue(
                    "Thông tin người dùng hoặc truyện không hợp lệ"
            );

            return;
        }

        if (!storyId.equals(currentStoryId)) {
            return;
        }

        currentProgressUserId = userId;

        progressLoading.setValue(true);
        readingProgress.setValue(null);

        readingRepository.getProgress(
                userId,
                storyId,
                new ReadingRepository.RepositoryCallback<ReadingProgressEntity>() {

                    @Override
                    public void onSuccess(ReadingProgressEntity result) {

                        if (!storyId.equals(currentStoryId)
                                || !userId.equals(currentProgressUserId)) {
                            return;
                        }

                        readingProgress.postValue(result);
                        progressLoading.postValue(false);
                        error.postValue(null);
                    }

                    @Override
                    public void onError(Exception e) {

                        if (!storyId.equals(currentStoryId)
                                || !userId.equals(currentProgressUserId)) {
                            return;
                        }

                        progressLoading.postValue(false);

                        error.postValue(
                                e != null && e.getMessage() != null
                                        ? e.getMessage()
                                        : "Không thể tải tiến độ đọc"
                        );
                    }
                }
        );
    }

    // ==========================================
    // SAVE READING HISTORY
    // ==========================================

    public void saveHistory(ReadingHistoryEntity history) {

        if (history == null) {
            return;
        }

        if (!isValidId(history.getUserId())
                || !isValidId(history.getStoryId())
                || !isValidId(history.getChapterId())) {

            error.setValue("Thông tin lịch sử đọc không hợp lệ");
            return;
        }

        readingRepository.insertHistory(
                history,
                null,
                e -> error.setValue(
                        e != null && e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể lưu lịch sử đọc"
                )
        );
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    // ==========================================
    // CLEAR ERROR
    // ==========================================

    public void clearError() {
        error.setValue(null);
    }
}
