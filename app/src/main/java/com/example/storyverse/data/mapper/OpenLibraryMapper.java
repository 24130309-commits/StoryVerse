
package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpenLibraryMapper {

    private OpenLibraryMapper() {
    }

    // Chuẩn hóa Unicode NFC, giữ nguyên dấu tiếng Việt
    private static String normalizeText(String value) {

        if (value == null) {
            return null;
        }

        return Normalizer.normalize(
                value.trim(),
                Normalizer.Form.NFC
        );
    }

    public static Story toStory(
            OpenLibraryResponse.BookItem item
    ) {

        if (item == null || item.key == null || item.title == null) {
            return null;
        }

        // Chuẩn hóa tiêu đề
        String title = normalizeText(item.title);

        // Lấy và chuẩn hóa tác giả
        String author = "Chưa rõ tác giả";

        if (item.authorNames != null && !item.authorNames.isEmpty()) {

            List<String> authors = new ArrayList<>();

            for (String name : item.authorNames) {

                String normalizedName = normalizeText(name);

                if (normalizedName != null && !normalizedName.isEmpty()) {
                    authors.add(normalizedName);
                }
            }

            if (!authors.isEmpty()) {
                author = android.text.TextUtils.join(", ", authors);
            }
        }

        // Lấy ảnh bìa
        String coverUrl = null;

        if (item.coverId != null) {
            coverUrl = "https://covers.openlibrary.org/b/id/"
                    + item.coverId + "-M.jpg";
        }

        // Lấy và chuẩn hóa ngôn ngữ
        String language = null;

        if (item.languages != null && !item.languages.isEmpty()) {

            List<String> languages = new ArrayList<>();

            for (String code : item.languages) {

                String normalizedCode = normalizeText(code);

                if (normalizedCode != null && !normalizedCode.isEmpty()) {
                    languages.add(normalizedCode.toLowerCase(Locale.ROOT));
                }
            }

            if (!languages.isEmpty()) {
                language = android.text.TextUtils.join(", ", languages);
            }
        }

        // Chuẩn hóa thể loại
        List<String> categories = new ArrayList<>();

        if (item.subjects != null) {

            for (String subject : item.subjects) {

                String normalizedSubject = normalizeText(subject);

                if (normalizedSubject != null && !normalizedSubject.isEmpty()) {
                    categories.add(normalizedSubject);
                }
            }
        }

        // Đường dẫn nguồn sách
        String sourceUrl = "https://openlibrary.org" + item.key;

        // Tạo ID riêng cho sách từ Open Library
        String storyId = "openlibrary_"
                + item.key.replace("/", "_");

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
