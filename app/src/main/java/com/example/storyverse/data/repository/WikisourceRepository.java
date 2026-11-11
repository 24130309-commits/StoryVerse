package com.example.storyverse.data.repository;

import com.example.storyverse.data.remote.api.RetrofitClient;
import com.example.storyverse.data.remote.api.wikisource.WikisourcePageResponse;
import com.example.storyverse.data.remote.api.wikisource.WikisourceSearchResponse;
import com.example.storyverse.data.remote.api.wikisource.WikisourceService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WikisourceRepository {

    private static final String BASE_URL =
            "https://vi.wikisource.org/";

    private final WikisourceService service;

    public WikisourceRepository() {
        service = RetrofitClient.createService(
                BASE_URL,
                WikisourceService.class
        );
    }

    // Tìm kiếm tác phẩm trên Wikisource
    public void searchPages(
            String keyword,
            int limit,
            RepositoryCallback<WikisourceSearchResponse> callback
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));

        Call<WikisourceSearchResponse> call =
                service.searchPages(
                        keyword.trim(),
                        0,
                        safeLimit
                );

        call.enqueue(new Callback<WikisourceSearchResponse>() {
            @Override
            public void onResponse(
                    Call<WikisourceSearchResponse> call,
                    Response<WikisourceSearchResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(
                            "Không thể tìm kiếm Wikisource. HTTP: "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<WikisourceSearchResponse> call,
                    Throwable t
            ) {
                callback.onError(
                        t.getMessage() != null
                                ? t.getMessage()
                                : "Lỗi kết nối Wikisource"
                );
            }
        });
    }

    // Lấy nội dung gốc của một trang Wikisource
    public void getPageContent(
            String title,
            RepositoryCallback<WikisourcePageResponse> callback
    ) {
        if (title == null || title.trim().isEmpty()) {
            callback.onError("Tên tác phẩm không được để trống");
            return;
        }

        Call<WikisourcePageResponse> call =
                service.getPageContent(
                        title.trim(),
                        5
                );

        call.enqueue(new Callback<WikisourcePageResponse>() {
            @Override
            public void onResponse(
                    Call<WikisourcePageResponse> call,
                    Response<WikisourcePageResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(
                            "Không thể lấy nội dung tác phẩm. HTTP: "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<WikisourcePageResponse> call,
                    Throwable t
            ) {
                callback.onError(
                        t.getMessage() != null
                                ? t.getMessage()
                                : "Lỗi kết nối Wikisource"
                );
            }
        });
    }

    // Callback dùng chung cho hai API
    public interface RepositoryCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }
}