
package com.example.storyverse.data.remote.api.googlebooks;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleBooksService {

    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String keyword,
            @Query("langRestrict") String language,
            @Query("maxResults") int maxResults,
            @Query("startIndex") int startIndex,
            @Query("key") String apiKey
    );

    @GET("volumes/{volumeId}")
    Call<GoogleBooksResponse.BookItem> getBookById(
            @Path("volumeId") String volumeId,
            @Query("key") String apiKey
    );
}
