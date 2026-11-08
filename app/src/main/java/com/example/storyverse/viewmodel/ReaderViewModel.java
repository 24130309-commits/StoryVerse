
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

import java.util.List;

public class ReaderViewModel extends AndroidViewModel {

    private final ChapterRepository chapterRepository;
    private final ReadingRepository readingRepository;

    // Danh sách chương và chương đang được đọc.
    private final MutableLiveData<List<ChapterEntity>> chapters =
            new MutableLiveData<>();

    private final MutableLiveData<ChapterEntity> currentChapter =
            new MutableLiveData<>();

    // Tiến độ đọc gần nhất của người dùng.
    private final MutableLiveData<ReadingProgressEntity> readingProgress =
            new MutableLiveData<>();

    // Trạng thái tải dữ liệu và thông báo lỗi.
    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    public ReaderViewModel(
            @NonNull Application application
    ) {

        super(application);

        chapterRepository =
                new ChapterRepository(application);

        readingRepository =
                new ReadingRepository(application);
    }

    // Cung cấp dữ liệu cho ReaderFragment theo dõi.

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

    public LiveData<String> getError() {
        return error;
    }

    // Tải danh sách chương theo truyện được chọn.

    public void loadChapters(String storyId) {

        if (storyId == null || storyId.isEmpty()) {

            error.setValue("Story ID không hợp lệ");
            return;
        }

        loading.postValue(true);

        chapterRepository.getChaptersByStoryId(
                storyId,
                new ChapterRepository.RepositoryCallback<List<ChapterEntity>>() {

                    @Override
                    public void onSuccess(
                            List<ChapterEntity> result
                    ) {

                        chapters.postValue(result);

                        if (result == null || result.isEmpty()) {

                            error.postValue(
                                    "Chưa có chương nào"
                            );

                        } else {

                            error.postValue(null);
                        }

                        loading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {

                        error.postValue(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Không thể tải danh sách chương"
                        );

                        loading.postValue(false);
                    }
                }
        );
    }

    // Chọn chương mà người đọc muốn mở.

    public void selectChapter(
            ChapterEntity chapter
    ) {

        if (chapter == null) {

            error.setValue("Chương không hợp lệ");
            return;
        }

        currentChapter.setValue(chapter);
        error.setValue(null);
    }

    // Chuyển sang chương kế tiếp trong danh sách.

    public void nextChapter() {

        List<ChapterEntity> chapterList =
                chapters.getValue();

        ChapterEntity current =
                currentChapter.getValue();

        if (chapterList == null ||
                chapterList.isEmpty()) {

            return;
        }

        if (current == null) {

            selectChapter(chapterList.get(0));
            return;
        }

        int currentIndex =
                chapterList.indexOf(current);

        if (currentIndex >= 0 &&
                currentIndex < chapterList.size() - 1) {

            ChapterEntity nextChapter =
                    chapterList.get(currentIndex + 1);

            currentChapter.setValue(nextChapter);

            error.setValue(null);

        } else {

            error.setValue(
                    "Bạn đã đọc đến chương cuối"
            );
        }
    }

    // Quay lại chương trước nếu vẫn còn chương để chuyển.

    public void previousChapter() {

        List<ChapterEntity> chapterList =
                chapters.getValue();

        ChapterEntity current =
                currentChapter.getValue();

        if (chapterList == null ||
                chapterList.isEmpty()) {

            return;
        }

        if (current == null) {

            selectChapter(chapterList.get(0));
            return;
        }

        int currentIndex =
                chapterList.indexOf(current);

        if (currentIndex > 0) {

            ChapterEntity previousChapter =
                    chapterList.get(currentIndex - 1);

            currentChapter.setValue(previousChapter);

            error.setValue(null);

        } else {

            error.setValue(
                    "Đây là chương đầu tiên"
            );
        }
    }

    // Lưu tiến độ đọc hiện tại.

    public void saveProgress(
            ReadingProgressEntity progress
    ) {

        if (progress == null) {
            return;
        }

        readingRepository.saveProgress(
                progress,
                () -> readingProgress.postValue(progress)
        );
    }

    // Tìm tiến độ đọc đã lưu để khôi phục lần đọc trước.

    public void loadProgress(
            String userId,
            String storyId
    ) {

        if (userId == null ||
                userId.isEmpty() ||
                storyId == null ||
                storyId.isEmpty()) {

            return;
        }

        readingRepository.getProgress(
                userId,
                storyId,
                new ReadingRepository.RepositoryCallback<ReadingProgressEntity>() {

                    @Override
                    public void onSuccess(
                            ReadingProgressEntity result
                    ) {

                        readingProgress.postValue(result);
                    }

                    @Override
                    public void onError(Exception e) {

                        error.postValue(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Không thể tải tiến độ đọc"
                        );
                    }
                }
        );
    }

    // Ghi lại chương mà người dùng đã đọc.

    public void saveHistory(
            ReadingHistoryEntity history
    ) {

        if (history == null) {
            return;
        }

        readingRepository.insertHistory(
                history,
                null
        );
    }

    // Tạo một số chương mẫu để kiểm tra chức năng đọc truyện.

    public void insertSampleChapters(String storyId) {

        if (storyId == null || storyId.isEmpty()) {
            return;
        }

        List<ChapterEntity> sampleChapters = new java.util.ArrayList<>();

        long currentTime = System.currentTimeMillis();

        sampleChapters.add(
                new ChapterEntity(
                        "chapter_demo_001",
                        storyId,
                        1,
                        "Ngày đầu tiên",
                        "Buổi sáng hôm ấy, thành phố vẫn còn chìm trong những tia nắng dịu dàng. " +
                                "Minh Thuận bước chậm trên con đường quen thuộc đến trường. " +
                                "Mọi thứ dường như vẫn bình thường, cho đến khi cậu vô tình gặp một cô gái đang đứng bên cạnh cổng trường.\n\n" +
                                "Cô gái ấy đang loay hoay tìm kiếm thứ gì đó trong chiếc túi nhỏ. " +
                                "Thuận nhìn thấy một quyển sách rơi dưới chân cô và nhẹ nhàng nhặt lên.\n\n" +
                                "Đó chỉ là một khoảnh khắc rất nhỏ, nhưng đôi khi những câu chuyện đẹp lại bắt đầu từ những điều giản dị như thế.",
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
                        "Ngày hôm sau, Thuận không nghĩ rằng mình sẽ gặp lại cô gái ấy nhanh đến vậy.\n\n" +
                                "Trong giờ nghỉ, cậu bước ra hành lang và nhìn thấy cô đang đứng một mình bên cửa sổ. " +
                                "Ánh nắng buổi trưa nhẹ nhàng rơi xuống mái tóc của cô.\n\n" +
                                "Hai người bắt đầu trò chuyện. Ban đầu chỉ là vài câu hỏi đơn giản về bài học và lớp học. " +
                                "Nhưng cuộc trò chuyện kéo dài lâu hơn cả hai người tưởng.\n\n" +
                                "Từ hôm đó, những lần gặp nhau tình cờ bắt đầu xuất hiện nhiều hơn.",
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
                        "Những ngày tiếp theo trôi qua nhẹ nhàng.\n\n" +
                                "Thuận và cô cùng nhau làm một bài tập nhóm. " +
                                "Những cuộc trò chuyện ngắn dần trở nên tự nhiên hơn.\n\n" +
                                "Có những lúc cả hai chỉ ngồi cạnh nhau mà không cần nói quá nhiều. " +
                                "Nhưng chính những khoảng lặng ấy lại khiến Thuận nhận ra rằng cô gái trước mặt đã trở nên đặc biệt với mình.\n\n" +
                                "Cậu bắt đầu mong chờ những ngày đến lớp, chỉ để có thể nhìn thấy cô thêm một lần.",
                        150,
                        currentTime,
                        currentTime,
                        currentTime
                )
        );

        chapterRepository.insertChapters(
                sampleChapters,
                () -> loadChapters(storyId)
        );
    }
}
