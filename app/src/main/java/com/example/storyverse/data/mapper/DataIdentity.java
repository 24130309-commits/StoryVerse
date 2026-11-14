
package com.example.storyverse.data.mapper;

import java.text.Normalizer;
import java.util.Locale;

public final class DataIdentity {

    private DataIdentity() {
    }

    public static String normalizeName(String value) {
        if (value == null) return "";

        String text = value.trim().toLowerCase(Locale.ROOT);
        text = text.replace("đ", "d");

        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}+", "");
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public static String toSlug(String value) {
        return normalizeName(value)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    public static String authorId(String name) {
        String slug = toSlug(name);
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Tên tác giả không hợp lệ");
        }
        return "author_" + slug;
    }

    public static String categoryId(String name) {
        String slug = toSlug(name);
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Tên thể loại không hợp lệ");
        }
        return "category_" + slug;
    }

    public static boolean sameName(String first, String second) {
        String a = normalizeName(first);
        String b = normalizeName(second);

        return !a.isEmpty() && a.equals(b);
    }
}
