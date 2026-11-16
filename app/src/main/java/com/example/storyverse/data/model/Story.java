package com.example.storyverse.data.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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
    private String category;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Kiểm tra truyện có khớp với cả từ khóa tìm kiếm và thể loại đang chọn hay không.
     */
    public boolean matchesSearch(String query, String selectedCategory) {
        return matchesCategory(selectedCategory) && matchesQuery(query);
    }

    /**
     * Lọc theo thể loại từ ChipGroup (hỗ trợ cả trường hợp 1 truyện có nhiều thể loại cách nhau bởi dấu phẩy).
     */
    public boolean matchesCategory(String selectedCategory) {
        if (selectedCategory == null || selectedCategory.trim().isEmpty()
                || selectedCategory.equalsIgnoreCase("Tất cả")) {
            return true;
        }
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        String normalizedTarget = normalizeText(selectedCategory);
        String[] categories = category.split(",");
        for (String item : categories) {
            if (normalizeText(item).equals(normalizedTarget)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tìm kiếm từ khóa theo tên truyện hoặc tên tác giả (hỗ trợ gõ có dấu và không dấu).
     */
    public boolean matchesQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String normalizedQuery = normalizeText(query);
        String normalizedTitle = normalizeText(getTitle());
        String normalizedAuthor = normalizeText(getAuthor());

        return normalizedTitle.contains(normalizedQuery)
                || normalizedAuthor.contains(normalizedQuery);
    }

    /**
     * Chuẩn hóa chuỗi về chữ thường và loại bỏ dấu tiếng Việt để so khớp linh hoạt.
     */
    private static String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim().toLowerCase(Locale.ROOT);
        String temp = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
}