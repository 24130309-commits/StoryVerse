package com.example.storyverse.data.repository;

import com.example.storyverse.data.mapper.NarouMapper;
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.RetrofitClient;
import com.example.storyverse.data.remote.api.narou.NarouService;
import com.google.gson.JsonArray;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository lấy metadata truyện từ Narou.
 */
public class NarouRepository {

    private static final String BASE_URL =
            "https://api.syosetu.com/";

    private static final String FIELDS =
            "t-n-w-s-bg-g-k-ga-l";

    private static final String SUPPORTED_GENRES =
            "101-102-201-202-302-303-304-305-306-307-401-402-403";

    private final NarouService service =
            RetrofitClient.createService(
                    BASE_URL,
                    NarouService.class
            );

    public interface CallbackResult {
        void onSuccess(List<Story> stories);

        void onError(String message);
    }

    public void searchWebNovels(
            String keyword,
            int page,
            int pageSize,
            CallbackResult callback
    ) {

        if (callback == null) {
            return;
        }

        int safePage = Math.max(1, page);
        int safeLimit = Math.max(1, Math.min(pageSize, 50));

        int start = ((safePage - 1) * safeLimit) + 1;

        service.searchNovels(
                "json",
                FIELDS,
                safeLimit,
                start,
                SUPPORTED_GENRES,
                keyword == null || keyword.trim().isEmpty()
                        ? null
                        : keyword.trim()
        ).enqueue(new Callback<JsonArray>() {

            @Override
            public void onResponse(
                    Call<JsonArray> call,
                    Response<JsonArray> response
            ) {

                if (!response.isSuccessful()
                        || response.body() == null) {

                    callback.onError(
                            "Narou API trả về HTTP "
                                    + response.code()
                    );

                    return;
                }

                List<Story> stories =
                        NarouMapper.toStoryList(response.body());

                if (stories.isEmpty()) {

                    callback.onError(
                            "Narou không trả về tác phẩm phù hợp"
                    );

                } else {

                    callback.onSuccess(stories);
                }
            }

            @Override
            public void onFailure(
                    Call<JsonArray> call,
                    Throwable error
            ) {

                callback.onError(
                        error != null && error.getMessage() != null
                                ? error.getMessage()
                                : "Không thể kết nối Narou API"
                );
            }
        });
    }
}