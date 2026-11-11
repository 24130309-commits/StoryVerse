package com.example.storyverse.data.repository;

import com.example.storyverse.data.remote.api.RetrofitClient;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenLibraryRepository {

    private static final String BASE_URL =
            "https://openlibrary.org/";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private static final String FIELDS =
            "key,title,author_name,first_publish_year,"
                    + "cover_i,language,subject,has_fulltext,ebook_access";

    private final OpenLibraryService apiService;

    public interface BooksCallback {
        void onSuccess(OpenLibraryResponse response);
        void onError(String message);
    }

    public OpenLibraryRepository() {
        apiService = RetrofitClient.createService(
                BASE_URL,
                OpenLibraryService.class
        );
    }

    // Giữ tương thích với code cũ.
    public void searchBooks(
            String keyword,
            BooksCallback callback
    ) {
        searchBooks(keyword, 1, DEFAULT_PAGE_SIZE, callback);
    }

    // page bắt đầu từ 1.
    public void searchBooks(
            String keyword,
            int page,
            int pageSize,
            BooksCallback callback
    ) {
        if (callback == null) return;

        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        if (page < 1) {
            callback.onError("Số trang phải lớn hơn hoặc bằng 1");
            return;
        }

        int safePageSize = Math.max(
                1,
                Math.min(pageSize, MAX_PAGE_SIZE)
        );

        apiService.searchBooks(
                keyword.trim(),
                FIELDS,
                safePageSize,
                page
        ).enqueue(new Callback<OpenLibraryResponse>() {

            @Override
            public void onResponse(
                    Call<OpenLibraryResponse> call,
                    Response<OpenLibraryResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(
                            "Open Library trả về lỗi HTTP: "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<OpenLibraryResponse> call,
                    Throwable throwable
            ) {
                callback.onError(
                        throwable.getMessage() != null
                                ? throwable.getMessage()
                                : "Không thể kết nối Open Library"
                );
            }
        });
    }
}