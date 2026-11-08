
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

    public void searchBooks(
            String keyword,
            BooksCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        apiService.searchBooks(
                keyword.trim(),
                "key,title,author_name,first_publish_year,cover_i,language,subject,has_fulltext,ebook_access",
                10,
                1
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
                            "Không thể lấy dữ liệu Open Library. Mã lỗi: "
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
