
package com.example.storyverse.data.remote.api.openlibrary;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenLibraryService {

    @GET("search.json")
    Call<OpenLibraryResponse> searchBooks(
            @Query("q") String keyword,
            @Query("fields") String fields,
            @Query("limit") int limit,
            @Query("page") int page
    );
}
