
package com.example.storyverse.data.remote.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL =
            "https://example.com/api/";

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL không được để trống");
        }

        String normalizedUrl = baseUrl.trim();

        if (!normalizedUrl.endsWith("/")) {
            normalizedUrl += "/";
        }

        return new Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = buildRetrofit(BASE_URL);
        }

        return retrofit;
    }

    public static <T> T createService(
            String baseUrl,
            Class<T> serviceClass
    ) {
        if (serviceClass == null) {
            throw new IllegalArgumentException("Service class không được null");
        }

        return buildRetrofit(baseUrl).create(serviceClass);
    }

    public static StoryApiService getStoryApiService() {
        return getInstance().create(StoryApiService.class);
    }
}
