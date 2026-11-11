package com.example.storyverse.data.repository;

import com.example.storyverse.data.remote.api.GutendexApiService;
import com.example.storyverse.data.remote.api.RetrofitClient;
import com.example.storyverse.data.remote.api.model.GutendexResponse;

import retrofit2.Callback;

public class GutendexRepository {

    private static final String BASE_URL =
            "https://gutendex.com/";

    private final GutendexApiService apiService;

    public GutendexRepository() {

        apiService = RetrofitClient.createService(
                BASE_URL,
                GutendexApiService.class
        );
    }

    public void getBooks(
            int page,
            String languages,
            String search,
            String topic,
            Callback<GutendexResponse> callback
    ) {

        apiService.getBooks(
                page,
                languages,
                "false",
                null,
                search,
                topic,
                "popular"
        ).enqueue(callback);
    }

    public void getPopularEnglishBooks(
            int page,
            Callback<GutendexResponse> callback
    ) {

        getBooks(
                page,
                "en",
                null,
                null,
                callback
        );
    }

    public void searchBooks(
            String keyword,
            int page,
            Callback<GutendexResponse> callback
    ) {

        getBooks(
                page,
                null,
                keyword,
                null,
                callback
        );
    }

    public void getBooksByTopic(
            String topic,
            int page,
            Callback<GutendexResponse> callback
    ) {

        getBooks(
                page,
                null,
                null,
                topic,
                callback
        );
    }
}