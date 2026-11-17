package com.example.storyverse.data.mapper;

import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.model.Story;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoryLocalMapper {

    private StoryLocalMapper() {
    }

    // =========================================
    // CHUẨN HÓA TÊN HIỂN THỊ
    // =========================================

    private static String normalizeDisplayName(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = Normalizer.normalize(
                value.trim(),
                Normalizer.Form.NFC
        );

        return normalized.replaceAll("\\s+", " ");
    }

    // =========================================
    // CHUẨN HÓA CHUỖI ĐỂ TẠO ID
    // =========================================

    private static String normalizeText(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String normalized = normalizeDisplayName(value)
                .toLowerCase(Locale.ROOT);

        normalized = Normalizer.normalize(
                normalized,
                Normalizer.Form.NFD
        );

        // Loại bỏ dấu Unicode.
        normalized = normalized.replaceAll("\\p{M}", "");

        // Chuyển đ thành d.
        normalized = normalized.replace('đ', 'd');

        return normalized;
    }

    // =========================================
    // TẠO ID ỔN ĐỊNH
    // =========================================

    private static String createId(
            String prefix,
            String value
    ) {

        String normalized = normalizeText(value);

        if (normalized.isEmpty()) {
            return null;
        }

        String idValue = normalized.replaceAll(
                "[^a-z0-9]+",
                "_"
        );

        idValue = idValue.replaceAll("^_+|_+$", "");

        if (idValue.isEmpty()) {
            return null;
        }

        return prefix + idValue;
    }

    // =========================================
    // CHUYỂN THÔNG TIN TÁC GIẢ
    // =========================================

    public static AuthorEntity toAuthorEntity(
            String authorName
    ) {

        String name = normalizeDisplayName(authorName);

        if (name.isEmpty()) {
            return null;
        }

        String authorId = createId("author_", name);

        if (authorId == null) {
            return null;
        }

        return new AuthorEntity(
                authorId,
                name,
                null,
                null
        );
    }

    // =========================================
    // CHUYỂN THÔNG TIN THỂ LOẠI
    // =========================================

    public static CategoryEntity toCategoryEntity(
            String categoryName
    ) {

        String name = normalizeDisplayName(categoryName);

        if (name.isEmpty()) {
            return null;
        }

        String categoryId = createId("category_", name);

        if (categoryId == null) {
            return null;
        }

        return new CategoryEntity(
                categoryId,
                name,
                null,
                null
        );
    }

    // =========================================
    // CHUYỂN THÔNG TIN TRUYỆN
    // =========================================

    public static StoryEntity toStoryEntity(Story story) {

        if (story == null
                || story.getStoryId() == null
                || story.getStoryId().trim().isEmpty()
                || story.getTitle() == null
                || story.getTitle().trim().isEmpty()) {

            return null;
        }

        AuthorEntity author =
                toAuthorEntity(story.getAuthor());

        String authorId = author != null
                ? author.getAuthorId()
                : null;

        long currentTime = System.currentTimeMillis();

        boolean isGutendexFullText =
                "Project Gutenberg (Gutendex)"
                        .equals(story.getSource());

        long chapterCount = isGutendexFullText ? 1 : 0;

        long previewChapterCount =
                isGutendexFullText ? 1 : 3;

        return new StoryEntity(
                story.getStoryId(),
                normalizeDisplayName(story.getTitle()),
                story.getDescription(),
                story.getCoverUrl(),
                authorId,
                "pending",
                story.getLanguage(),
                null,
                0,
                0,
                0.0,
                0,
                chapterCount,
                previewChapterCount,
                story.getSource(),
                story.getSourceUrl(),
                story.getLicense(),
                currentTime,
                currentTime
        );
    }

    // =========================================
    // CHUYỂN DANH SÁCH THỂ LOẠI
    // =========================================

    public static List<CategoryEntity> toCategoryEntities(
            Story story
    ) {

        Map<String, CategoryEntity> categories =
                new LinkedHashMap<>();

        if (story == null || story.getCategories() == null) {
            return new ArrayList<>();
        }

        for (String categoryName : story.getCategories()) {

            CategoryEntity category =
                    toCategoryEntity(categoryName);

            if (category != null) {

                categories.put(
                        category.getCategoryId(),
                        category
                );
            }
        }

        return new ArrayList<>(categories.values());
    }

    // =========================================
    // TẠO LIÊN KẾT TRUYỆN - THỂ LOẠI
    // =========================================

    public static List<StoryCategoryEntity>
    toStoryCategoryEntities(Story story) {

        List<StoryCategoryEntity> relations =
                new ArrayList<>();

        if (story == null
                || story.getStoryId() == null
                || story.getStoryId().trim().isEmpty()) {

            return relations;
        }

        List<CategoryEntity> categories =
                toCategoryEntities(story);

        for (CategoryEntity category : categories) {

            relations.add(
                    new StoryCategoryEntity(
                            story.getStoryId(),
                            category.getCategoryId()
                    )
            );
        }

        return relations;
    }
}