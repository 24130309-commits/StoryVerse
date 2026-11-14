
package com.example.storyverse.data.remote.api.openlibrary;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenLibraryResponse {

    @SerializedName("numFound")
    public int numFound;

    @SerializedName("start")
    public int start;

    @SerializedName("docs")
    public List<BookItem> docs;

    public static class BookItem {

        @SerializedName("key")
        public String key;

        @SerializedName("title")
        public String title;

        @SerializedName("author_name")
        public List<String> authorNames;

        @SerializedName("first_publish_year")
        public Integer firstPublishYear;

        @SerializedName("cover_i")
        public Integer coverId;

        @SerializedName("language")
        public List<String> languages;

        @SerializedName("subject")
        public List<String> subjects;

        @SerializedName("has_fulltext")
        public Boolean hasFullText;

        @SerializedName("ebook_access")
        public String ebookAccess;
    }
}
