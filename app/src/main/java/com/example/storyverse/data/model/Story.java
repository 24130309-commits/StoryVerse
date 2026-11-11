package com.example.storyverse.data.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Story {

    private String storyId;
    private String title;
    private String author;
    private String description;
    private String coverUrl;
    private String language;
    private List<String> categories;
    private String source;
    private String sourceUrl;
    private String license;

    public Story() {
    }

    // Constructor tương thích phiên bản cũ
    public Story(
            String storyId,
            String title,
            String author,
            String description,
            String coverUrl,
            String language,
            List<String> categories,
            String source,
            String sourceUrl
    ) {
        this(
                storyId,
                title,
                author,
                description,
                coverUrl,
                language,
                categories,
                source,
                sourceUrl,
                null
        );
    }

    // Constructor đầy đủ có thông tin giấy phép
    public Story(
            String storyId,
            String title,
            String author,
            String description,
            String coverUrl,
            String language,
            List<String> categories,
            String source,
            String sourceUrl,
            String license
    ) {
        this.storyId = storyId;
        setTitle(title);
        setAuthor(author);
        setDescription(description);
        this.coverUrl = coverUrl;
        setLanguage(language);
        setCategories(categories);
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.license = license;
    }

    private String normalizeVietnamese(String value) {
        if (value == null) {
            return null;
        }

        return Normalizer.normalize(
                value,
                Normalizer.Form.NFC
        );
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalizeVietnamese(title);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = normalizeVietnamese(author);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalizeVietnamese(description);
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = normalizeVietnamese(language);
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        if (categories == null) {
            this.categories = null;
            return;
        }

        this.categories = new ArrayList<>();

        for (String category : categories) {
            this.categories.add(
                    normalizeVietnamese(category)
            );
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}