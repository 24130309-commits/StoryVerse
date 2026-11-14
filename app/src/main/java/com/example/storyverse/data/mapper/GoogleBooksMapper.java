
package com.example.storyverse.data.mapper;

import android.text.TextUtils;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GoogleBooksMapper {

    private GoogleBooksMapper() {
    }

    // =====================================================
    // 1. CHUẨN HÓA VĂN BẢN
    // =====================================================

    private static String normalizeText(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String result = Normalizer.normalize(
                value.trim(),
                Normalizer.Form.NFC
        );

        result = result.replaceAll("\\s+", " ");

        return result.isEmpty() ? null : result;
    }

    // =====================================================
    // 2. TẠO KHÓA SO SÁNH
    // =====================================================

    private static String normalizeKey(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String result = value.trim().toLowerCase(Locale.ROOT);

        result = result.replace('đ', 'd');

        result = Normalizer.normalize(
                result,
                Normalizer.Form.NFD
        );

        result = result.replaceAll("\\p{M}+", "");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    // =====================================================
    // 3. CHUẨN HÓA URL ẢNH
    // =====================================================

    private static String normalizeImageUrl(String url) {

        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String result = url.trim();

        result = result.replaceFirst("^http://", "https://");

        return result;
    }

    // =====================================================
    // 4. CHUYỂN GOOGLE BOOKS THÀNH STORY
    // =====================================================

    public static Story toStory(
            GoogleBooksResponse.BookItem item
    ) {

        if (item == null
                || item.volumeInfo == null
                || item.id == null
                || item.id.trim().isEmpty()) {

            return null;
        }

        GoogleBooksResponse.VolumeInfo info = item.volumeInfo;

        String title = normalizeText(info.title);

        if (title == null) {
            return null;
        }

        // -------------------------------------------------
        // TÁC GIẢ
        // -------------------------------------------------

        String author = null;

        if (info.authors != null) {

            List<String> authors = new ArrayList<>();
            Set<String> authorKeys = new HashSet<>();

            for (String name : info.authors) {

                String normalizedName = normalizeText(name);

                if (normalizedName == null) {
                    continue;
                }

                String key = normalizeKey(normalizedName);

                if (!key.isEmpty() && authorKeys.add(key)) {
                    authors.add(normalizedName);
                }
            }

            if (!authors.isEmpty()) {
                author = TextUtils.join(", ", authors);
            }
        }

        // -------------------------------------------------
        // MÔ TẢ
        // -------------------------------------------------

        String description = normalizeText(info.description);

        // -------------------------------------------------
        // ẢNH BÌA
        // -------------------------------------------------

        String coverUrl = null;

        if (info.imageLinks != null) {

            coverUrl = normalizeImageUrl(
                    info.imageLinks.thumbnail
            );

            if (coverUrl == null) {
                coverUrl = normalizeImageUrl(
                        info.imageLinks.small
                );
            }
        }

        // -------------------------------------------------
        // THỂ LOẠI
        // -------------------------------------------------

        List<String> categories = new ArrayList<>();
        Set<String> categoryKeys = new HashSet<>();

        if (info.categories != null) {

            for (String category : info.categories) {

                String normalizedCategory = normalizeText(category);

                if (normalizedCategory == null) {
                    continue;
                }

                String key = normalizeKey(normalizedCategory);

                if (!key.isEmpty() && categoryKeys.add(key)) {
                    categories.add(normalizedCategory);
                }
            }
        }

        // -------------------------------------------------
        // NGÔN NGỮ
        // -------------------------------------------------

        String language = normalizeText(info.language);

        if (language != null) {
            language = language.toLowerCase(Locale.ROOT);
        }

        // -------------------------------------------------
        // NGUỒN DỮ LIỆU
        // -------------------------------------------------

        String sourceUrl =
                "https://books.google.com/books?id=" + item.id;

        // Giữ nguyên quy tắc ID hiện tại.
        String storyId = "google_" + item.id.trim();

        // -------------------------------------------------
        // TẠO STORY
        // -------------------------------------------------

        return new Story(
                storyId,
                title,
                author,
                description,
                coverUrl,
                language,
                categories,
                "Google Books",
                sourceUrl
        );
    }

    // =====================================================
    // 5. CHUYỂN DANH SÁCH BOOK ITEM THÀNH STORY
    // =====================================================

    public static List<Story> toStoryList(
            List<GoogleBooksResponse.BookItem> items
    ) {

        List<Story> stories = new ArrayList<>();

        if (items == null) {
            return stories;
        }

        for (GoogleBooksResponse.BookItem item : items) {

            Story story = toStory(item);

            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }
}
