
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderViewModel extends AndroidViewModel {

    private final ChapterRepository chapterRepository;
    private final ReadingRepository readingRepository;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<ChapterEntity>> chapters =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<ChapterEntity> currentChapter =
            new MutableLiveData<>();

    private final MutableLiveData<ReadingProgressEntity> readingProgress =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

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

    // LiveData

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

    // Home compatibility

    public void setCurrentChapter(ChapterEntity chapter) {
        currentChapter.setValue(chapter);
    }

    public void setReadingProgress(ReadingProgressEntity progress) {
        readingProgress.setValue(progress);
    }

    // Load chapters

    public void loadChapters(String storyId) {

        if (!isValidId(storyId)) {
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

    // Select chapter

    public void selectChapter(ChapterEntity chapter) {

        if (chapter == null) {
            error.setValue("Chương không hợp lệ");
            return;
        }

        currentChapter.setValue(chapter);
        error.setValue(null);
    }

    // Find chapter index

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

    // Next chapter

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

    // Previous chapter

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

    // Save reading progress

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

        executorService.execute(() -> {

            try {
                readingRepository.saveProgress(progress);
                readingProgress.postValue(progress);

            } catch (Exception e) {
                error.postValue(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể lưu tiến độ đọc"
                );
            }
        });
    }

    // Load reading progress

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

        executorService.execute(() -> {

            try {
                ReadingProgressEntity result =
                        readingRepository.getProgress(userId, storyId);

                if (!storyId.equals(currentStoryId)
                        || !userId.equals(currentProgressUserId)) {
                    return;
                }

                readingProgress.postValue(result);
                progressLoading.postValue(false);
                error.postValue(null);

            } catch (Exception e) {

                if (!storyId.equals(currentStoryId)
                        || !userId.equals(currentProgressUserId)) {
                    return;
                }

                progressLoading.postValue(false);

                error.postValue(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể tải tiến độ đọc"
                );
            }
        });
    }

    // Save reading history

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

        executorService.execute(() -> {

            try {
                readingRepository.insertHistory(history);

            } catch (Exception e) {

                error.postValue(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể lưu lịch sử đọc"
                );
            }
        });
    }

    // Insert sample chapters

    public void insertSampleChapters(String storyId) {

        if (!isValidId(storyId)) {
            return;
        }

        List<ChapterEntity> sampleChapters = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        sampleChapters.add(
                new ChapterEntity(
                        "chapter_demo_001",
                        storyId,
                        1,
                        "Ngày đầu tiên",
                        "Buổi sáng hôm ấy, thành phố vẫn còn chìm trong những tia nắng dịu dàng. "
                                + "Minh Thuận bước chậm trên con đường quen thuộc đến trường. "
                                + "Mọi thứ dường như vẫn bình thường, cho đến khi cậu vô tình gặp một cô gái đang đứng bên cạnh cổng trường.\n\n"
                                + "Cô gái ấy đang loay hoay tìm kiếm thứ gì đó trong chiếc túi nhỏ. "
                                + "Thuận nhìn thấy một quyển sách rơi dưới chân cô và nhẹ nhàng nhặt lên.\n\n"
                                + "Đó chỉ là một khoảnh khắc rất nhỏ, nhưng đôi khi những câu chuyện đẹp lại bắt đầu từ những điều giản dị như thế.",
                        120,
                        currentTime,
                        currentTime,
                        currentTime
                )
        );

        sampleChapters.add(
                new ChapterEntity(
                        "chapter_demo_002",
                        storyId,
                        2,
                        "Một lần gặp lại",
                        "Ngày hôm sau, Thuận không nghĩ rằng mình sẽ gặp lại cô gái ấy nhanh đến vậy.\n\n"
                                + "Trong giờ nghỉ, cậu bước ra hành lang và nhìn thấy cô đang đứng một mình bên cửa sổ. "
                                + "Ánh nắng buổi trưa nhẹ nhàng rơi xuống mái tóc của cô.\n\n"
                                + "Hai người bắt đầu trò chuyện. Ban đầu chỉ là vài câu hỏi đơn giản về bài học và lớp học. "
                                + "Nhưng cuộc trò chuyện kéo dài lâu hơn cả hai người tưởng.\n\n"
                                + "Từ hôm đó, những lần gặp nhau tình cờ bắt đầu xuất hiện nhiều hơn.",
                        135,
                        currentTime,
                        currentTime,
                        currentTime
                )
        );

        sampleChapters.add(
                new ChapterEntity(
                        "chapter_demo_003",
                        storyId,
                        3,
                        "Khoảng cách gần hơn",
                        "Những ngày tiếp theo trôi qua nhẹ nhàng.\n\n"
                                + "Thuận và cô cùng nhau làm một bài tập nhóm. "
                                + "Những cuộc trò chuyện ngắn dần trở nên tự nhiên hơn.\n\n"
                                + "Có những lúc cả hai chỉ ngồi cạnh nhau mà không cần nói quá nhiều. "
                                + "Nhưng chính những khoảng lặng ấy lại khiến Thuận nhận ra rằng cô gái trước mặt đã trở nên đặc biệt với mình.\n\n"
                                + "Cậu bắt đầu mong chờ những ngày đến lớp, chỉ để có thể nhìn thấy cô thêm một lần.",
                        150,
                        currentTime,
                        currentTime,
                        currentTime
                )
        );

        chapterRepository.insertChapters(
                sampleChapters,
                () -> loadChapters(storyId),
                e -> error.postValue(
                        e != null && e.getMessage() != null
                                ? e.getMessage()
                                : "Không thể tạo chương mẫu"
                )
        );
    }

    // Validation

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    // Clear error

    public void clearError() {
        error.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
