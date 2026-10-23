package com.example.storyverse.data.remote.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StoryApiService {

    @GET("stories")
    Call<Object> getStories();

    @GET("stories/{storyId}")
    Call<Object> getStoryById(
            @Path("storyId") String storyId
    );

    @GET("stories/search")
    Call<Object> searchStories(
            @Query("keyword") String keyword
    );
}