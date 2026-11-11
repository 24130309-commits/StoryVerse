package com.example.storyverse.data.remote.api.wikisource;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WikisourceService {

    @Headers({
            "User-Agent: STORYVERSE/1.0 (https://github.com/24130309-commits/StoryVerse)"
    })
    @GET("w/api.php?action=query&list=search&format=json&formatversion=2")
    Call<WikisourceSearchResponse> searchPages(
            @Query("srsearch") String keyword,
            @Query("srnamespace") int namespace,
            @Query("srlimit") int limit
    );

    @Headers({
            "User-Agent: STORYVERSE/1.0 (https://github.com/24130309-commits/StoryVerse)"
    })
    @GET("w/api.php?action=query&prop=revisions&rvprop=content&rvslots=main&format=json&formatversion=2")
    Call<WikisourcePageResponse> getPageContent(
            @Query("titles") String title,
            @Query("maxlag") int maxlag
    );
}