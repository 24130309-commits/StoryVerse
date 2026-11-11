package com.example.storyverse.data.remote.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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

        return retrofitMap.computeIfAbsent(url, key -> {

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            return new Retrofit.Builder()
                    .baseUrl(key)
                    .client(client)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();
        });
    }

    public static <T> T createService(
            String baseUrl,
            Class<T> serviceClass
    ) {
        return getInstance(baseUrl).create(serviceClass);
    }
}