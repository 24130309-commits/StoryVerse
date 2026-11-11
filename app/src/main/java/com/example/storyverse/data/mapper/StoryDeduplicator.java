package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StoryDeduplicator {

    private StoryDeduplicator() {
    }

    // =========================================
    // 1. CHUẨN HÓA DỮ LIỆU ĐỂ SO SÁNH
    // =========================================

    private static String normalize(String value) {

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
        result = result.replaceAll("[^a-z0-9]", "");

        return result;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // =========================================
    // 2. TẠO KHÓA NHẬN DIỆN TRUYỆN
    // =========================================

    private static String createKey(Story story) {

        if (story == null) {
            return "";
        }

        String title = normalize(story.getTitle());
        String author = normalize(story.getAuthor());

        if (title.isEmpty()) {
            return "";
        }

        // Có tác giả: so sánh tên truyện và tác giả.
        if (!author.isEmpty()) {
            return "WORK|" + title + "|" + author;
        }

        // Không có tác giả: dùng ID nguồn để tránh
        // gộp nhầm các truyện có cùng tên.
        String storyId = story.getStoryId();

        if (hasValue(storyId)) {
            return "SOURCE_ID|"
                    + storyId.trim().toLowerCase(Locale.ROOT);
        }

        return "";
    }

    // =========================================
    // 3. ĐÁNH GIÁ CHẤT LƯỢNG BẢN GHI
    // =========================================

    private static int calculateQuality(Story story) {

        if (story == null) {
            return -1;
        }

        int score = 0;

        if (hasValue(story.getTitle())) score += 2;
        if (hasValue(story.getAuthor())) score += 2;
        if (hasValue(story.getDescription())) score += 2;
        if (hasValue(story.getCoverUrl())) score += 2;
        if (hasValue(story.getLanguage())) score++;
        if (hasValue(story.getSource())) score++;
        if (hasValue(story.getSourceUrl())) score++;
        if (hasValue(story.getLicense())) score++;

        if (story.getCategories() != null) {

            for (String category : story.getCategories()) {

                if (hasValue(category)) {
                    score++;
                }
            }
        }

        return score;
    }

    // =========================================
    // 4. ƯU TIÊN NGUỒN DỮ LIỆU
    // =========================================

    private static int getSourcePriority(Story story) {

        if (story == null || story.getSource() == null) {
            return 0;
        }

        String source = story.getSource().trim();

        if (source.equalsIgnoreCase("Google Books")) {
            return 2;
        }

        if (source.equalsIgnoreCase("Open Library")) {
            return 1;
        }

        return 0;
    }

    private static Story choosePreferred(
            Story first,
            Story second
    ) {

        int firstQuality = calculateQuality(first);
        int secondQuality = calculateQuality(second);

        if (firstQuality > secondQuality) {
            return first;
        }

        if (secondQuality > firstQuality) {
            return second;
        }

        return getSourcePriority(first) >= getSourcePriority(second)
                ? first
                : second;
    }

    // =========================================
    // 5. GỘP THỂ LOẠI
    // =========================================

    private static List<String> mergeCategories(
            Story first,
            Story second
    ) {

        Map<String, String> categories = new LinkedHashMap<>();

        if (first != null) {
            addCategories(categories, first.getCategories());
        }

        if (second != null) {
            addCategories(categories, second.getCategories());
        }

        return new ArrayList<>(categories.values());
    }

    private static void addCategories(
            Map<String, String> target,
            List<String> source
    ) {

        if (source == null) {
            return;
        }

        for (String category : source) {

            if (!hasValue(category)) {
                continue;
            }

            String key = normalize(category);

            if (!key.isEmpty() && !target.containsKey(key)) {
                target.put(key, category.trim());
            }
        }
    }

    // =========================================
    // 6. LẤY DỮ LIỆU ĐẦY ĐỦ HƠN
    // =========================================

    private static String firstNonEmpty(
            String first,
            String second
    ) {
        return hasValue(first) ? first : second;
    }

    private static Story mergeStories(
            Story preferred,
            Story secondary
    ) {

        if (preferred == null) {
            return secondary;
        }

        if (secondary == null) {
            return preferred;
        }

        return new Story(
                preferred.getStoryId(),

                firstNonEmpty(
                        preferred.getTitle(),
                        secondary.getTitle()
                ),

                firstNonEmpty(
                        preferred.getAuthor(),
                        secondary.getAuthor()
                ),

                firstNonEmpty(
                        preferred.getDescription(),
                        secondary.getDescription()
                ),

                firstNonEmpty(
                        preferred.getCoverUrl(),
                        secondary.getCoverUrl()
                ),

                firstNonEmpty(
                        preferred.getLanguage(),
                        secondary.getLanguage()
                ),

                mergeCategories(preferred, secondary),

                firstNonEmpty(
                        preferred.getSource(),
                        secondary.getSource()
                ),

                firstNonEmpty(
                        preferred.getSourceUrl(),
                        secondary.getSourceUrl()
                ),

                firstNonEmpty(
                        preferred.getLicense(),
                        secondary.getLicense()
                )
        );
    }

    // =========================================
    // 7. LOẠI BỎ TRUYỆN TRÙNG
    // =========================================

    public static List<Story> removeDuplicates(
            List<Story> stories
    ) {

        Map<String, Story> uniqueStories = new LinkedHashMap<>();

        // Các bản ghi không đủ dữ liệu nhận diện vẫn được giữ lại.
        List<Story> unidentifiedStories = new ArrayList<>();

        if (stories == null || stories.isEmpty()) {
            return new ArrayList<>();
        }

        for (Story story : stories) {

            if (story == null) {
                continue;
            }

            String key = createKey(story);

            // Không đủ thông tin để nhận diện:
            // giữ lại thay vì tự ý xóa.
            if (key.isEmpty()) {
                unidentifiedStories.add(story);
                continue;
            }

            Story existing = uniqueStories.get(key);

            if (existing == null) {
                uniqueStories.put(key, story);
                continue;
            }

            Story preferred = choosePreferred(
                    existing,
                    story
            );

            Story secondary = preferred == existing
                    ? story
                    : existing;

            uniqueStories.put(
                    key,
                    mergeStories(preferred, secondary)
            );
        }

        List<Story> result =
                new ArrayList<>(uniqueStories.values());

        result.addAll(unidentifiedStories);

        return result;
    }
}