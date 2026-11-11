package com.example.storyverse.data.remote.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class GutendexBook {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("authors")
    private List<GutendexPerson> authors;

    @SerializedName("summaries")
    private List<String> summaries;

    @SerializedName("subjects")
    private List<String> subjects;

    @SerializedName("bookshelves")
    private List<String> bookshelves;

    @SerializedName("languages")
    private List<String> languages;

    @SerializedName("copyright")
    private Boolean copyright;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("formats")
    private Map<String, String> formats;

    @SerializedName("download_count")
    private int downloadCount;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<GutendexPerson> getAuthors() {
        return authors;
    }

    public List<String> getSummaries() {
        return summaries;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<String> getBookshelves() {
        return bookshelves;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public Boolean getCopyright() {
        return copyright;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Map<String, String> getFormats() {
        return formats;
    }

    public int getDownloadCount() {
        return downloadCount;
    }
}