
package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleBooksMapper {

    private GoogleBooksMapper() {
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
            GoogleBooksResponse.BookItem item
    ) {

        if (item == null || item.volumeInfo == null) {
            return null;
        }

        GoogleBooksResponse.VolumeInfo info = item.volumeInfo;

        // Chuẩn hóa tiêu đề
        String title = normalizeText(info.title);

        // Lấy và chuẩn hóa tác giả
        String author = "Chưa rõ tác giả";

        if (info.authors != null && !info.authors.isEmpty()) {
            List<String> authors = new ArrayList<>();

            for (String name : info.authors) {
                String normalizedName = normalizeText(name);

                if (normalizedName != null && !normalizedName.isEmpty()) {
                    authors.add(normalizedName);
                }
            }

            if (!authors.isEmpty()) {
                author = android.text.TextUtils.join(", ", authors);
            }
        }

        // Chuẩn hóa mô tả
        String description = normalizeText(info.description);

        // Lấy ảnh bìa
        String coverUrl = null;

        if (info.imageLinks != null) {

            if (info.imageLinks.thumbnail != null) {
                coverUrl = info.imageLinks.thumbnail
                        .replace("http://", "https://");

            } else if (info.imageLinks.small != null) {
                coverUrl = info.imageLinks.small
                        .replace("http://", "https://");
            }
        }

        // Chuẩn hóa thể loại
        List<String> categories = new ArrayList<>();

        if (info.categories != null) {

            for (String category : info.categories) {
                String normalizedCategory = normalizeText(category);

                if (normalizedCategory != null
                        && !normalizedCategory.isEmpty()) {
                    categories.add(normalizedCategory);
                }
            }
        }

        // Chuẩn hóa mã ngôn ngữ
        String language = normalizeText(info.language);

        if (language != null) {
            language = language.toLowerCase(java.util.Locale.ROOT);
        }

        // Đường dẫn nguồn sách
        String sourceUrl = item.id != null
                ? "https://books.google.com/books?id=" + item.id
                : null;

        return new Story(
                "google_" + item.id,
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
