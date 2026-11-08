package com.example.storyverse.data.remote.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final Map<String, Retrofit> retrofitMap =
            new ConcurrentHashMap<>();

    private RetrofitClient() {
        // Prevent creating object
    }

    public static Retrofit getInstance(String baseUrl) {

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Base URL không được để trống"
            );
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        final String url = baseUrl;

        return retrofitMap.computeIfAbsent(url, key ->
                new Retrofit.Builder()
                        .baseUrl(key)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        )
                        .build()
        );
    }

    public static <T> T createService(
            String baseUrl,
            Class<T> serviceClass
    ) {
        return getInstance(baseUrl).create(serviceClass);
    }
}