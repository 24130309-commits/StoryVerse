package com.example.storyverse;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;
import com.example.storyverse.data.repository.GoogleBooksRepository;
import com.example.storyverse.data.repository.OpenLibraryRepository;
import com.example.storyverse.data.repository.MultiSourceStoryRepository;
import com.example.storyverse.data.repository.StoryRepository;
import com.example.storyverse.data.repository.ChapterRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GoogleBooksTest";
    private static final String OPEN_LIBRARY_TAG = "OpenLibraryTest";
    private static final String MULTI_SOURCE_TAG = "MultiSourceTest";
    private static final String FIRESTORE_TAG = "FirestoreRoomTest";
    private static final String CHAPTER_TAG = "ChapterFirestoreTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Kiểm tra kết hợp Google Books và Open Library
        testMultiSourceApi();

        // Kiểm tra tải dữ liệu Story từ Firestore và lưu vào Room
        testFirestoreToRoom();

        // Kiểm tra tải dữ liệu Chapter từ Firestore và lưu vào Room
        testChapterFirestoreToRoom();
    }

    // ==========================================
    // KIỂM TRA GOOGLE BOOKS RIÊNG
    // ==========================================

    private void testGoogleBooksApi() {

        GoogleBooksRepository repository =
                new GoogleBooksRepository();

        repository.searchBooks(
                "truyện",
                new GoogleBooksRepository.BooksCallback() {

                    @Override
                    public void onSuccess(GoogleBooksResponse response) {

                        Log.d(TAG, "Kết nối Google Books thành công!");
                        Log.d(TAG, "Tổng số kết quả: " + response.totalItems);

                        if (response.items != null) {
                            for (GoogleBooksResponse.BookItem item : response.items) {

                                if (item.volumeInfo != null) {
                                    Log.d(TAG,
                                            "Tên sách: " + item.volumeInfo.title);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Lỗi Google Books: " + message);
                    }
                }
        );
    }

    // ==========================================
    // KIỂM TRA OPEN LIBRARY RIÊNG
    // ==========================================

    private void testOpenLibraryApi() {

        OpenLibraryRepository repository =
                new OpenLibraryRepository();

        repository.searchBooks(
                "truyện Việt Nam",
                new OpenLibraryRepository.BooksCallback() {

                    @Override
                    public void onSuccess(OpenLibraryResponse response) {

                        Log.d(OPEN_LIBRARY_TAG,
                                "Kết nối Open Library thành công!");

                        Log.d(OPEN_LIBRARY_TAG,
                                "Tổng số kết quả: " + response.numFound);

                        if (response.docs != null) {
                            for (OpenLibraryResponse.BookItem book : response.docs) {

                                Log.d(OPEN_LIBRARY_TAG,
                                        "Tên sách: " + book.title);

                                Log.d(OPEN_LIBRARY_TAG,
                                        "Tác giả: " + book.authorNames);
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(OPEN_LIBRARY_TAG,
                                "Lỗi Open Library: " + message);
                    }
                }
        );
    }

    // ==========================================
    // KIỂM TRA KẾT HỢP NHIỀU NGUỒN API
    // ==========================================

    private void testMultiSourceApi() {

        MultiSourceStoryRepository repository =
                new MultiSourceStoryRepository();

        repository.searchStories(
                "truyện",
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {

                        Log.d(MULTI_SOURCE_TAG,
                                "Kết hợp API thành công!");

                        Log.d(MULTI_SOURCE_TAG,
                                "Tổng số sách: " + stories.size());

                        for (Story story : stories) {

                            Log.d(MULTI_SOURCE_TAG,
                                    "-------------------------");

                            Log.d(MULTI_SOURCE_TAG,
                                    "Tên sách: " + story.getTitle());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Tác giả: " + story.getAuthor());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Ngôn ngữ: " + story.getLanguage());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Thể loại: " + story.getCategories());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Nguồn: " + story.getSource());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Link nguồn: " + story.getSourceUrl());

                            Log.d(MULTI_SOURCE_TAG,
                                    "Ảnh bìa: " + story.getCoverUrl());
                        }

                        // Hiển thị cảnh báo nếu một nguồn API bị lỗi
                        for (String warning : warnings) {
                            Log.w(MULTI_SOURCE_TAG, warning);
                        }
                    }

                    @Override
                    public void onError(String message) {

                        Log.e(MULTI_SOURCE_TAG,
                                "Lỗi kết hợp API: " + message);
                    }
                }
        );
    }

    // ==========================================
    // KIỂM TRA FIRESTORE -> ROOM (STORY)
    // ==========================================

    private void testFirestoreToRoom() {

        Log.d(FIRESTORE_TAG,
                "Bắt đầu kiểm tra Firestore -> Room");

        StoryRepository repository =
                new StoryRepository(getApplicationContext());

        repository.fetchAndCacheStories(
                new StoryRepository.RepositoryCallback<List<StoryEntity>>() {

                    @Override
                    public void onSuccess(List<StoryEntity> stories) {

                        Log.d(FIRESTORE_TAG,
                                "Tải dữ liệu Firestore thành công!");

                        Log.d(FIRESTORE_TAG,
                                "Số truyện nhận được: " + stories.size());

                        for (StoryEntity story : stories) {

                            Log.d(FIRESTORE_TAG,
                                    "-------------------------");

                            Log.d(FIRESTORE_TAG,
                                    "ID: " + story.getStoryId());

                            Log.d(FIRESTORE_TAG,
                                    "Tên truyện: " + story.getTitle());

                            Log.d(FIRESTORE_TAG,
                                    "Tác giả ID: " + story.getAuthorId());
                        }

                        repository.getCachedStoryCount(
                                new StoryRepository.RepositoryCallback<Integer>() {

                                    @Override
                                    public void onSuccess(Integer count) {

                                        Log.d(FIRESTORE_TAG,
                                                "Lưu dữ liệu vào Room thành công!");

                                        Log.d(FIRESTORE_TAG,
                                                "Tổng số truyện đang lưu trong Room: "
                                                        + count);
                                    }

                                    @Override
                                    public void onError(Exception e) {

                                        Log.e(FIRESTORE_TAG,
                                                "Lỗi kiểm tra số lượng truyện trong Room: "
                                                        + e.getMessage(),
                                                e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(FIRESTORE_TAG,
                                "Lỗi tải hoặc lưu dữ liệu Firestore: "
                                        + e.getMessage(),
                                e);
                    }
                }
        );
    }

    // ==========================================
    // KIỂM TRA FIRESTORE -> ROOM (CHAPTER)
    // ==========================================

    private void testChapterFirestoreToRoom() {

        final String STORY_ID = "story_001";

        Log.d(CHAPTER_TAG,
                "Bắt đầu kiểm tra Firestore -> Room cho Chapter");

        ChapterRepository repository =
                new ChapterRepository(getApplicationContext());

        // Bước 1: Tải chương từ Firestore và lưu vào Room
        repository.fetchAndCacheChapters(
                STORY_ID,

                // Khi tải và lưu thành công
                () -> {

                    Log.d(CHAPTER_TAG,
                            "Tải và lưu chương thành công!");

                    // Bước 2: Kiểm tra số chương đã lưu trong Room
                    repository.getChapterCountByStoryId(
                            STORY_ID,
                            new ChapterRepository.RepositoryCallback<Integer>() {

                                @Override
                                public void onSuccess(Integer count) {

                                    Log.d(CHAPTER_TAG,
                                            "Tổng số chương trong Room: " + count);

                                    // Bước 3: Đọc danh sách chương từ Room
                                    repository.getChaptersByStoryId(
                                            STORY_ID,
                                            new ChapterRepository.RepositoryCallback<List<ChapterEntity>>() {

                                                @Override
                                                public void onSuccess(
                                                        List<ChapterEntity> chapters) {

                                                    Log.d(CHAPTER_TAG,
                                                            "Đọc danh sách chương từ Room thành công!");

                                                    Log.d(CHAPTER_TAG,
                                                            "Số chương đọc được: "
                                                                    + chapters.size());

                                                    for (ChapterEntity chapter : chapters) {

                                                        Log.d(CHAPTER_TAG,
                                                                "-------------------------");

                                                        Log.d(CHAPTER_TAG,
                                                                "Chapter ID: "
                                                                        + chapter.getChapterId());

                                                        Log.d(CHAPTER_TAG,
                                                                "Story ID: "
                                                                        + chapter.getStoryId());

                                                        Log.d(CHAPTER_TAG,
                                                                "Số chương: "
                                                                        + chapter.getChapterNumber());

                                                        Log.d(CHAPTER_TAG,
                                                                "Tiêu đề: "
                                                                        + chapter.getTitle());

                                                        Log.d(CHAPTER_TAG,
                                                                "Nội dung: "
                                                                        + chapter.getContent());
                                                    }
                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                    Log.e(CHAPTER_TAG,
                                                            "Lỗi đọc danh sách chương từ Room: "
                                                                    + e.getMessage(),
                                                            e);
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onError(Exception e) {

                                    Log.e(CHAPTER_TAG,
                                            "Lỗi kiểm tra số chương trong Room: "
                                                    + e.getMessage(),
                                            e);
                                }
                            }
                    );
                },

                // Khi tải hoặc lưu dữ liệu thất bại
                error -> Log.e(CHAPTER_TAG,
                        "Lỗi tải hoặc lưu Chapter: " + error.getMessage(),
                        error)
        );
    }
}