
package com.example.storyverse;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;
import com.example.storyverse.data.repository.GoogleBooksRepository;
import com.example.storyverse.data.repository.OpenLibraryRepository;
import com.example.storyverse.data.repository.MultiSourceStoryRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GoogleBooksTest";
    private static final String OPEN_LIBRARY_TAG = "OpenLibraryTest";
    private static final String MULTI_SOURCE_TAG = "MultiSourceTest";

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
                                            "Tên sách: " + item.volumeInfo.title
                                    );
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
}
