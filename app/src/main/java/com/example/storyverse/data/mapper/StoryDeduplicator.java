
package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoryDeduplicator {

    private StoryDeduplicator() {
    }

    // Chuẩn hóa chuỗi để so sánh
    private static String normalize(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        // Chuyển về chữ thường
        String result = value.toLowerCase(Locale.ROOT);

        // Loại bỏ dấu tiếng Việt
        result = Normalizer.normalize(
                result,
                Normalizer.Form.NFD
        );

        result = result.replaceAll("\\p{M}", "");

        // Xử lý riêng chữ đ
        result = result.replace('đ', 'd');

        // Loại bỏ ký tự đặc biệt và khoảng trắng thừa
        result = result.replaceAll("[^a-z0-9]", "");

        return result;
    }

    // Tạo khóa nhận diện sách
    private static String createKey(Story story) {

        String title = normalize(story.getTitle());
        String author = normalize(story.getAuthor());

        if (title.isEmpty()) {
            return "";
        }

        return title + "|" + author;
    }

    // Loại bỏ sách trùng
    public static List<Story> removeDuplicates(
            List<Story> stories
    ) {

        Map<String, Story> uniqueStories = new LinkedHashMap<>();

        if (stories == null) {
            return new ArrayList<>();
        }

        for (Story story : stories) {

            if (story == null) {
                continue;
            }

            String key = createKey(story);

            if (key.isEmpty()) {
                continue;
            }

            // Chỉ giữ bản ghi đầu tiên
            if (!uniqueStories.containsKey(key)) {
                uniqueStories.put(key, story);
            }
        }

        return new ArrayList<>(uniqueStories.values());
    }
}
