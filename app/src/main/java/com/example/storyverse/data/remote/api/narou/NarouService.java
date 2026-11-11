package com.example.storyverse.data.remote.api.narou;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API lấy metadata truyện từ Narou.
 * Không tải nội dung chương.
 */
public interface NarouService {

    @GET("novelapi/api/")
    Call<JsonArray> searchNovels(
            @Query("out") String output,
            @Query("of") String fields,
            @Query("lim") int limit,
            @Query("st") int start,
            @Query("genre") String genres,
            @Query("word") String keyword
    );
}