package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.model.GutendexBook;
import com.example.storyverse.data.remote.api.model.GutendexPerson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Chuyển metadata từ Gutendex thành Story model.
 */
public final class GutendexMapper {

    private GutendexMapper() {
    }

    public static Story toStory(GutendexBook book) {

        if (book == null
                || book.getId() <= 0
                || isBlank(book.getTitle())) {
            return null;
        }

        // Chỉ nhận sách được đánh dấu không còn bản quyền
        // và có định dạng text/plain.
        if (!Boolean.FALSE.equals(book.getCopyright())
                || findPlainTextUrl(book) == null) {
            return null;
        }

        // -----------------------------------------
        // AUTHORS
        // -----------------------------------------

        List<String> authors = new ArrayList<>();

        if (book.getAuthors() != null) {
            for (GutendexPerson person : book.getAuthors()) {

                if (person != null
                        && !isBlank(person.getName())) {

                    authors.add(person.getName().trim());
                }
            }
        }

        // -----------------------------------------
        // DESCRIPTION
        // -----------------------------------------

        List<String> summaries = book.getSummaries();

        String description =
                summaries == null || summaries.isEmpty()
                        ? null
                        : summaries.get(0);

        // -----------------------------------------
        // CATEGORIES
        // -----------------------------------------

        Set<String> categorySet = new LinkedHashSet<>();

        addValues(categorySet, book.getSubjects());
        addValues(categorySet, book.getBookshelves());

        // -----------------------------------------
        // LANGUAGE
        // -----------------------------------------

        String language =
                book.getLanguages() == null
                        || book.getLanguages().isEmpty()
                        ? null
                        : join(book.getLanguages());

        // -----------------------------------------
        // COVER IMAGE
        // -----------------------------------------

        String coverUrl = null;

        Map<String, String> formats = book.getFormats();

        if (formats != null) {

            coverUrl = formats.get("image/jpeg");

            if (isBlank(coverUrl)) {

                for (Map.Entry<String, String> entry
                        : formats.entrySet()) {

                    if (entry.getKey() != null
                            && entry.getKey()
                            .toLowerCase(Locale.ROOT)
                            .startsWith("image/jpeg")
                            && !isBlank(entry.getValue())) {

                        coverUrl = entry.getValue();
                        break;
                    }
                }
            }
        }

        // -----------------------------------------
        // SOURCE INFORMATION
        // -----------------------------------------

        String sourceUrl =
                "https://www.gutenberg.org/ebooks/"
                        + book.getId();

        return new Story(
                "gutendex_" + book.getId(),
                book.getTitle().trim(),
                authors.isEmpty() ? null : join(authors),
                description,
                coverUrl,
                language,
                new ArrayList<>(categorySet),
                "Project Gutenberg (Gutendex)",
                sourceUrl,
                "Public domain - Project Gutenberg; verify local jurisdiction"
        );
    }

    // =========================================
    // CHUYỂN DANH SÁCH SÁCH
    // =========================================

    public static List<Story> toStoryList(
            List<GutendexBook> books
    ) {

        List<Story> stories = new ArrayList<>();

        if (books == null) {
            return stories;
        }

        for (GutendexBook book : books) {

            Story story = toStory(book);

            if (story != null) {
                stories.add(story);
            }
        }

        return stories;
    }

    // =========================================
    // TÌM LINK VĂN BẢN THUẦN
    // =========================================

    public static String findPlainTextUrl(
            GutendexBook book
    ) {

        if (book == null || book.getFormats() == null) {
            return null;
        }

        Map<String, String> formats = book.getFormats();

        for (Map.Entry<String, String> entry
                : formats.entrySet()) {

            String mime = entry.getKey();

            if (mime != null
                    && mime.toLowerCase(Locale.ROOT)
                    .startsWith("text/plain")
                    && !isBlank(entry.getValue())) {

                return entry.getValue();
            }
        }

        return null;
    }

    // =========================================
    // HỖ TRỢ XỬ LÝ DỮ LIỆU
    // =========================================

    private static void addValues(
            Set<String> target,
            List<String> values
    ) {

        if (values == null) {
            return;
        }

        for (String value : values) {

            if (!isBlank(value)) {
                target.add(value.trim());
            }
        }
    }

    private static String join(List<String> values) {

        StringBuilder result = new StringBuilder();

        for (String value : values) {

            if (isBlank(value)) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }

            result.append(value.trim());
        }

        return result.length() == 0
                ? null
                : result.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}