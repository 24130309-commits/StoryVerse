package com.example.storyverse.data.remote.api.wikisource;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WikisourceSearchResponse {

    @SerializedName("query")
    public Query query;

    public static class Query {
        @SerializedName("search")
        public List<SearchPage> search;
    }

    public static class SearchPage {
        @SerializedName("pageid")
        public long pageId;

        @SerializedName("title")
        public String title;

        @SerializedName("size")
        public long size;

        @SerializedName("timestamp")
        public String timestamp;

        @SerializedName("snippet")
        public String snippet;
    }
}