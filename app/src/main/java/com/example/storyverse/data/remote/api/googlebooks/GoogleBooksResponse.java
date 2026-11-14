
package com.example.storyverse.data.remote.api.googlebooks;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoogleBooksResponse {

    @SerializedName("totalItems")
    public int totalItems;

    @SerializedName("items")
    public List<BookItem> items;

    public static class BookItem {

        @SerializedName("id")
        public String id;

        @SerializedName("volumeInfo")
        public VolumeInfo volumeInfo;
    }

    public static class VolumeInfo {

        @SerializedName("title")
        public String title;

        @SerializedName("authors")
        public List<String> authors;

        @SerializedName("description")
        public String description;

        @SerializedName("publishedDate")
        public String publishedDate;

        @SerializedName("categories")
        public List<String> categories;

        @SerializedName("language")
        public String language;

        @SerializedName("imageLinks")
        public ImageLinks imageLinks;
    }

    public static class ImageLinks {

        @SerializedName("thumbnail")
        public String thumbnail;

        @SerializedName("small")
        public String small;

        @SerializedName("medium")
        public String medium;

        @SerializedName("large")
        public String large;
    }
}
