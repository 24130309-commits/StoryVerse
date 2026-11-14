package com.example.storyverse.data.repository;

import com.example.storyverse.BuildConfig;
import com.example.storyverse.data.remote.api.RetrofitClient;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleBooksRepository {

    private static final String BASE_URL =
            "https://www.googleapis.com/books/v1/";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 40;

    private final GoogleBooksService apiService;
    private final String apiKey = BuildConfig.BOOKS_API_KEY;

    public interface BooksCallback {
        void onSuccess(GoogleBooksResponse response);

        void onError(String message);
    }

    public GoogleBooksRepository() {

        apiService = RetrofitClient.createService(
                BASE_URL,
                GoogleBooksService.class
        );
    }

    // Tương thích với cách gọi cũ.
    public void searchBooks(
            String keyword,
            BooksCallback callback
    ) {

        searchBooks(
                keyword,
                1,
                DEFAULT_PAGE_SIZE,
                callback
        );
    }

    // page bắt đầu từ 1.
    public void searchBooks(
            String keyword,
            int page,
            int pageSize,
            BooksCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {

            callback.onError(
                    "Từ khóa tìm kiếm không được để trống"
            );

            return;
        }

        if (page < 1) {

            callback.onError(
                    "Số trang phải lớn hơn hoặc bằng 1"
            );

            return;
        }

        int safePageSize = Math.max(
                1,
                Math.min(pageSize, MAX_PAGE_SIZE)
        );

        int startIndex = (page - 1) * safePageSize;

        apiService.searchBooks(
                keyword.trim(),
                "vi",
                safePageSize,
                startIndex,
                apiKey == null || apiKey.trim().isEmpty()
                        ? null
                        : apiKey.trim()
        ).enqueue(new Callback<GoogleBooksResponse>() {

            @Override
            public void onResponse(
                    Call<GoogleBooksResponse> call,
                    Response<GoogleBooksResponse> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    callback.onSuccess(response.body());

                } else {

                    callback.onError(
                            "Google Books trả về lỗi HTTP: "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<GoogleBooksResponse> call,
                    Throwable throwable
            ) {

                callback.onError(
                        throwable.getMessage() != null
                                ? throwable.getMessage()
                                : "Không thể kết nối Google Books"
                );
            }
        });
    }
}