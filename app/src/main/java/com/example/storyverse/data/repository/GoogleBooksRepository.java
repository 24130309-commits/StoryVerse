
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

    public void searchBooks(String keyword, BooksCallback callback) {

        if (callback == null) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("Chưa cấu hình Google Books API Key");
            return;
        }

        apiService.searchBooks(
                keyword.trim(),
                "vi",
                20,
                0,
                apiKey
        ).enqueue(new Callback<GoogleBooksResponse>() {

            @Override
            public void onResponse(
                    Call<GoogleBooksResponse> call,
                    Response<GoogleBooksResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(
                            "Không thể lấy dữ liệu sách. Mã lỗi: "
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
                                : "Không thể kết nối đến Google Books"
                );
            }
        });
    }
}
