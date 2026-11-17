package com.example.storyverse.data.mapper;

import com.example.storyverse.data.model.Story;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Chuyển metadata từ Narou thành Story.
 * Không sao chép nội dung chương.
 */
public final class NarouMapper {

    private NarouMapper() {
    }

    public static List<Story> toStoryList(JsonArray response) {

        List<Story> stories = new ArrayList<>();

        if (response == null) {
            return stories;
        }

        // -----------------------------------------
        // DUYỆT DANH SÁCH TRUYỆN
        // -----------------------------------------

        for (JsonElement element : response) {

            if (element == null || !element.isJsonObject()) {
                continue;
            }

            JsonObject item = element.getAsJsonObject();

            String ncode = text(item, "ncode");
            String title = text(item, "title");

            if (isBlank(ncode) || isBlank(title)) {
                continue;
            }

            String writer = text(item, "writer");
            String synopsis = text(item, "story");

            int genre = integer(item, "genre");

            String keywords = text(item, "keyword");

            Set<String> categories =
                    mapGenres(genre, keywords);

            String normalizedCode =
                    ncode.trim().toLowerCase(Locale.ROOT);

            stories.add(
                    new Story(
                            "narou_" + normalizedCode,
                            title.trim(),
                            writer,
                            synopsis,
                            null,
                            "ja",
                            new ArrayList<>(categories),
                            "Narou",
                            "https://ncode.syosetu.com/"
                                    + normalizedCode
                                    + "/",
                            null
                    )
            );
        }

        return stories;
    }

    // =========================================
    // ÁNH XẠ THỂ LOẠI
    // =========================================

    private static Set<String> mapGenres(
            int code,
            String keywords
    ) {

        Set<String> result = new LinkedHashSet<>();

        switch (code) {

            case 101:
                result.add("Ngôn tình");
                result.add("Dị giới");
                break;

            case 102:
                result.add("Ngôn tình");
                result.add("Đô thị");
                break;

            case 201:
                result.add("Huyền huyễn");
                result.add("Phiêu lưu");
                break;

            case 202:
                result.add("Huyền huyễn");
                break;

            case 302:
                result.add("Tâm lý");
                break;

            case 303:
                result.add("Lịch sử");
                break;

            case 304:
                result.add("Trinh thám");
                break;

            case 305:
                result.add("Kinh dị");
                break;

            case 306:
                result.add("Hành động");
                result.add("Phiêu lưu");
                break;

            case 307:
                result.add("Hài hước");
                break;

            case 401:
            case 402:
            case 403:
                result.add("Khoa huyễn");
                break;

            default:
                break;
        }

        // -----------------------------------------
        // ÁNH XẠ TỪ TỪ KHÓA
        // -----------------------------------------

        String words = keywords == null
                ? ""
                : keywords.toLowerCase(Locale.ROOT);

        // Trọng sinh
        if (words.contains("転生")
                || words.contains("reincarnation")
                || words.contains("rebirth")) {

            result.add("Trọng sinh");
        }

        // Dị giới và xuyên không
        if (words.contains("転移")
                || words.contains("異世界")
                || words.contains("isekai")
                || words.contains("transmigration")) {

            result.add("Dị giới");
            result.add("Xuyên không");
        }

        // Học đường và thanh xuân
        if (words.contains("学園")
                || words.contains("school")
                || words.contains("青春")) {

            result.add("Học đường");
            result.add("Thanh xuân");
        }

        // Đam mỹ
        if (words.contains("bl")
                || words.contains("boys love")
                || words.contains("ボーイズラブ")) {

            result.add("Đam mỹ");
        }

        // Cung đấu
        if (words.contains("宮廷")
                || words.contains("後宮")
                || words.contains("palace")) {

            result.add("Cung đấu");
        }

        // Quân sự
        if (words.contains("軍事")
                || words.contains("military")) {

            result.add("Quân sự");
        }

        return result;
    }

    // =========================================
    // ĐỌC STRING TỪ JSON
    // =========================================

    private static String text(
            JsonObject object,
            String field
    ) {

        if (object == null
                || !object.has(field)
                || object.get(field).isJsonNull()) {

            return null;
        }

        try {
            return object.get(field).getAsString();

        } catch (Exception ignored) {
            return null;
        }
    }

    // =========================================
    // ĐỌC INTEGER TỪ JSON
    // =========================================

    private static int integer(
            JsonObject object,
            String field
    ) {

        if (object == null
                || !object.has(field)
                || object.get(field).isJsonNull()) {

            return 0;
        }

        try {
            return object.get(field).getAsInt();

        } catch (Exception ignored) {
            return 0;
        }
    }

    // =========================================
    // KIỂM TRA CHUỖI RỖNG
    // =========================================

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}