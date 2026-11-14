
package com.example.storyverse.data.mapper;

import android.text.TextUtils;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OpenLibraryMapper {

    private OpenLibraryMapper() {
    }

    // =====================================================
    // 1. CHUẨN HÓA UNICODE VÀ KHOẢNG TRẮNG
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

        // Chuẩn hóa chữ đ trước khi loại bỏ dấu.
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
    // 3. CHUẨN HÓA MÃ NGÔN NGỮ
    // =====================================================

    private static String normalizeLanguage(String code) {

        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String result = code.trim().toLowerCase(Locale.ROOT);

        // Open Library có thể trả về dạng /languages/vie.
        if (result.contains("/")) {
            String[] parts = result.split("/");

            result = parts[parts.length - 1];
        }

        result = result.trim();

        return result.isEmpty() ? null : result;
    }

    // =====================================================
    // 4. CHUYỂN OPEN LIBRARY BOOK ITEM THÀNH STORY
    // =====================================================

    public static Story toStory(
            OpenLibraryResponse.BookItem item
    ) {

        if (item == null) {
            return null;
        }

        String key = normalizeText(item.key);
        String title = normalizeText(item.title);

        if (key == null || title == null) {
            return null;
        }

        // -------------------------------------------------
        // TÁC GIẢ
        // -------------------------------------------------

        String author = null;

        if (item.authorNames != null) {

            List<String> authors = new ArrayList<>();
            Set<String> authorKeys = new HashSet<>();

            for (String name : item.authorNames) {

                String normalizedName = normalizeText(name);

                if (normalizedName == null) {
                    continue;
                }

                String comparisonKey = normalizeKey(normalizedName);

                if (!comparisonKey.isEmpty()
                        && authorKeys.add(comparisonKey)) {

                    authors.add(normalizedName);
                }
            }

            if (!authors.isEmpty()) {
                author = TextUtils.join(", ", authors);
            }
        }

        // -------------------------------------------------
        // ẢNH BÌA
        // -------------------------------------------------

        String coverUrl = null;

        if (item.coverId != null && item.coverId > 0) {

            coverUrl =
                    "https://covers.openlibrary.org/b/id/"
                            + item.coverId
                            + "-M.jpg";
        }

        // -------------------------------------------------
        // NGÔN NGỮ
        // -------------------------------------------------

        String language = null;

        if (item.languages != null) {

            List<String> languages = new ArrayList<>();
            Set<String> languageKeys = new HashSet<>();

            for (String code : item.languages) {

                String normalizedCode = normalizeLanguage(code);

                if (normalizedCode != null
                        && languageKeys.add(normalizedCode)) {

                    languages.add(normalizedCode);
                }
            }

            if (!languages.isEmpty()) {
                language = TextUtils.join(", ", languages);
            }
        }

        // -------------------------------------------------
        // THỂ LOẠI
        // -------------------------------------------------

        List<String> categories = new ArrayList<>();

        if (item.subjects != null) {

            Set<String> categoryKeys = new HashSet<>();

            for (String subject : item.subjects) {

                String normalizedSubject = normalizeText(subject);

                if (normalizedSubject == null) {
                    continue;
                }

                String comparisonKey = normalizeKey(normalizedSubject);

                if (!comparisonKey.isEmpty()
                        && categoryKeys.add(comparisonKey)) {

                    categories.add(normalizedSubject);
                }
            }
        }

        // -------------------------------------------------
        // ĐƯỜNG DẪN NGUỒN
        // -------------------------------------------------

        String sourceUrl = "https://openlibrary.org" + key;

        // -------------------------------------------------
        // ID
        // Giữ nguyên quy tắc tạo ID hiện tại.
        // -------------------------------------------------

        String storyId = "openlibrary_"
                + key.replace("/", "_");

        // -------------------------------------------------
        // TẠO STORY
        // -------------------------------------------------

        return new Story(
                storyId,
                title,
                author,
                null,
                coverUrl,
                language,
                categories,
                "Open Library",
                sourceUrl
        );
    }

    // =====================================================
    // 5. CHUYỂN DANH SÁCH BOOK ITEM THÀNH STORY
    // =====================================================

    public static List<Story> toStoryList(
            List<OpenLibraryResponse.BookItem> items
    ) {

        List<Story> stories = new ArrayList<>();

        if (items == null) {
            return stories;
        }

        for (OpenLibraryResponse.BookItem item : items) {

            Story story = toStory(item);

            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }
}
