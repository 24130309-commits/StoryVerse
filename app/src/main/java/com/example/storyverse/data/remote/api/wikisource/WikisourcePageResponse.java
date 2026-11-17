package com.example.storyverse.data.remote.api.wikisource;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WikisourcePageResponse {

    @SerializedName("query")
    public Query query;

    public static class Query {
        @SerializedName("pages")
        public List<Page> pages;
    }

    public static class Page {
        @SerializedName("pageid")
        public long pageId;

        @SerializedName("title")
        public String title;

        @SerializedName("revisions")
        public List<Revision> revisions;
    }

    public static class Revision {
        @SerializedName("slots")
        public Slots slots;
    }

    public static class Slots {
        @SerializedName("main")
        public MainSlot main;
    }

    public static class MainSlot {
        @SerializedName("content")
        public String content;
    }
}