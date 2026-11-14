package com.example.storyverse.data.remote.api;

import com.example.storyverse.data.remote.api.model.GutendexResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GutendexApiService {

    @GET("books")
    Call<GutendexResponse> getBooks(
            @Query("page") Integer page,
            @Query("languages") String languages,
            @Query("copyright") String copyright,
            @Query("mime_type") String mimeType,
            @Query("search") String search,
            @Query("topic") String topic,
            @Query("sort") String sort
    );
}