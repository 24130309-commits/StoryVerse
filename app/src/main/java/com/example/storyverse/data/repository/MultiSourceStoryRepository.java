
package com.example.storyverse.data.repository;

import android.text.TextUtils;

import com.example.storyverse.data.mapper.GoogleBooksMapper;
import com.example.storyverse.data.mapper.OpenLibraryMapper;
import com.example.storyverse.data.mapper.StoryDeduplicator;
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MultiSourceStoryRepository {

    private final GoogleBooksRepository googleBooksRepository;
    private final OpenLibraryRepository openLibraryRepository;

    public interface MultiSourceCallback {
        void onSuccess(List<Story> stories, List<String> warnings);

        void onError(String message);
    }

    public MultiSourceStoryRepository() {
        googleBooksRepository = new GoogleBooksRepository();
        openLibraryRepository = new OpenLibraryRepository();
    }

    public void searchStories(
            String keyword,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        final List<Story> allStories = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();

        final Object lock = new Object();
        final int[] completedRequests = {0};

        Runnable finishRequest = () -> {
            synchronized (lock) {
                completedRequests[0]++;

                if (completedRequests[0] == 2) {

                    if (!allStories.isEmpty()) {

                        // Bước 1: Loại bỏ sách trùng từ hai nguồn API
                        List<Story> uniqueStories =
                                StoryDeduplicator.removeDuplicates(allStories);

                        // Bước 2: Lọc sách có ngôn ngữ tiếng Việt
                        List<Story> vietnameseStories = new ArrayList<>();

                        for (Story story : uniqueStories) {
                            if (isVietnamese(story)) {
                                vietnameseStories.add(story);
                            }
                        }

                        // Bước 3: Trả kết quả
                        if (!vietnameseStories.isEmpty()) {

                            callback.onSuccess(
                                    vietnameseStories,
                                    new ArrayList<>(warnings)
                            );

                        } else {
                            callback.onError(
                                    "Không tìm thấy sách có ngôn ngữ tiếng Việt"
                            );
                        }

                    } else {
                        String errorMessage = warnings.isEmpty()
                                ? "Không tìm thấy sách"
                                : TextUtils.join("; ", warnings);

                        callback.onError(errorMessage);
                    }
                }
            }
        };

        // Nguồn 1: Google Books
        googleBooksRepository.searchBooks(
                keyword,
                new GoogleBooksRepository.BooksCallback() {

                    @Override
                    public void onSuccess(GoogleBooksResponse response) {

                        List<Story> stories =
                                GoogleBooksMapper.toStoryList(response.items);

                        synchronized (lock) {
                            allStories.addAll(stories);
                        }

                        finishRequest.run();
                    }

                    @Override
                    public void onError(String message) {

                        synchronized (lock) {
                            warnings.add("Google Books: " + message);
                        }

                        finishRequest.run();
                    }
                }
        );

        // Nguồn 2: Open Library
        openLibraryRepository.searchBooks(
                keyword,
                new OpenLibraryRepository.BooksCallback() {

                    @Override
                    public void onSuccess(OpenLibraryResponse response) {

                        List<Story> stories =
                                OpenLibraryMapper.toStoryList(response.docs);

                        synchronized (lock) {
                            allStories.addAll(stories);
                        }

                        finishRequest.run();
                    }

                    @Override
                    public void onError(String message) {

                        synchronized (lock) {
                            warnings.add("Open Library: " + message);
                        }

                        finishRequest.run();
                    }
                }
        );
    }

    // Kiểm tra sách có ngôn ngữ tiếng Việt hay không
    private boolean isVietnamese(Story story) {

        if (story == null || story.getLanguage() == null) {
            return false;
        }

        String language = story.getLanguage()
                .trim()
                .toLowerCase(Locale.ROOT);

        String[] languages = language.split("[,;\\s]+");

        for (String code : languages) {

            // Chấp nhận mã ngôn ngữ tiếng Việt
            if (code.equals("vi") || code.equals("vie")) {
                return true;
            }
        }

        return false;
    }
}
