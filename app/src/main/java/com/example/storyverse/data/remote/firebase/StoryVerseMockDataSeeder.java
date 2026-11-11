
package com.example.storyverse.data.remote.firebase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tạo bộ dữ liệu mẫu 360 truyện cho STORYVERSE trên Cloud Firestore.
 * Chỉ chạy thủ công từ chức năng debug, không tự động chạy khi mở ứng dụng.
 * Không xóa dữ liệu cũ.
 */
public final class StoryVerseMockDataSeeder {

    private static final String VERSION = "storyverse_catalog_360_v1";
    private static final int BATCH_LIMIT = 400;
    private static final int CHAPTERS_PER_STORY = 3;

    private static final CategorySeed[] CATEGORIES = {
            new CategorySeed("category_ngon_tinh", "Ngôn tình", 45),
            new CategorySeed("category_tien_hiep", "Tiên hiệp", 35),
            new CategorySeed("category_kiem_hiep", "Kiếm hiệp", 30),
            new CategorySeed("category_huyen_huyen", "Kỳ ảo", 30),
            new CategorySeed("category_trinh_tham", "Trinh thám", 25),
            new CategorySeed("category_kinh_di", "Kinh dị", 20),
            new CategorySeed("category_do_thi", "Đô thị", 25),
            new CategorySeed("category_xuyen_khong", "Xuyên không", 25),
            new CategorySeed("category_trong_sinh", "Trọng sinh", 20),
            new CategorySeed("category_hoc_duong", "Học đường", 20),
            new CategorySeed("category_hanh_dong", "Hành động", 20),
            new CategorySeed("category_khoa_hoc_vien_tuong", "Khoa học viễn tưởng", 15),
            new CategorySeed("category_hai_huoc", "Hài hước", 15),
            new CategorySeed("category_lich_su", "Lịch sử", 10),
            new CategorySeed("category_phieu_luu", "Phiêu lưu", 15),
            new CategorySeed("category_tam_ly", "Tâm lý", 10)
    };

    private static final String[] AUTHOR_NAMES = {
            "Nguyễn Minh An", "Trần Bảo Ngọc", "Lê Hoàng Phúc",
            "Phạm Gia Hân", "Võ Nhật Nam", "Đặng Khánh Linh",
            "Bùi Hoàng Long", "Đỗ Mai Anh", "Phan Quốc Bảo",
            "Hồ Ngọc Hà", "Ngô Tuấn Kiệt", "Dương Thuỳ Dương",
            "Lý Gia Huy", "Mai Thanh Tâm", "Cao Nhật Minh",
            "Vũ Phương Thảo", "Trịnh Đức Anh", "Tạ Minh Châu",
            "Đinh Quang Huy", "Lâm Bảo Trân", "Nguyễn Hải Đăng",
            "Trần Ngọc Ánh", "Lê Thành Đạt", "Phạm Yến Nhi",
            "Võ Anh Khoa", "Đặng Minh Khang", "Bùi Khánh Vy",
            "Đỗ Gia Bảo", "Phan Tú Uyên", "Hồ Nhật Quang"
    };

    private static final String[] TITLE_PREFIXES = {
            "Khúc Hát", "Bí Mật", "Hành Trình", "Ánh Trăng",
            "Dấu Chân", "Lời Hẹn", "Miền Ký Ức", "Ngày Tháng",
            "Vùng Đất", "Cánh Cửa", "Bản Giao Hưởng", "Mật Mã",
            "Ngọn Gió", "Định Mệnh", "Chân Trời", "Mùa Xuân",
            "Thành Phố", "Giấc Mơ"
    };

    private static final String[] TITLE_SUFFIXES = {
            "Cuối Cùng", "Bị Lãng Quên", "Sau Cơn Mưa",
            "Ở Phía Bên Kia", "Không Tên", "Của Ngày Mai",
            "Trong Sương Mù", "Và Lời Hứa", "Giữa Hai Thế Giới",
            "Trở Lại", "Dưới Bầu Trời Xa", "Của Người Lữ Khách",
            "Nơi Bình Minh Đến", "Từ Quá Khứ", "Khi Đêm Xuống",
            "Của Những Vì Sao"
    };

    private StoryVerseMockDataSeeder() {
    }

    public interface Callback {
        void onComplete(boolean success, String message);
    }

    /**
     * Nạp 360 truyện lên Firestore nếu bộ dữ liệu chưa được tạo.
     */
    public static void seed360(Callback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("system").document(VERSION).get()
                .addOnSuccessListener(marker -> {

                    if (marker.exists()) {
                        complete(callback, true,
                                "Kho dữ liệu 360 truyện đã được nạp trước đó.");
                        return;
                    }

                    try {
                        BatchWriter writer = new BatchWriter(db);
                        long now = System.currentTimeMillis();

                        addCategories(db, writer);
                        addAuthors(db, writer);
                        addStoriesAndChapters(db, writer, now);

                        writer.commitAll()
                                .addOnSuccessListener(unused ->
                                        writeMarker(db, now, callback))
                                .addOnFailureListener(error ->
                                        complete(callback, false,
                                                "Không thể ghi dữ liệu catalog: "
                                                        + error.getMessage()));

                    } catch (Exception error) {
                        complete(callback, false,
                                "Không thể tạo dữ liệu: " + error.getMessage());
                    }
                })
                .addOnFailureListener(error ->
                        complete(callback, false,
                                "Không thể kiểm tra trạng thái seeder: "
                                        + error.getMessage()));
    }

    /**
     * Tạo 16 thể loại truyện.
     */
    private static void addCategories(
            FirebaseFirestore db,
            BatchWriter writer
    ) {
        for (CategorySeed category : CATEGORIES) {

            Map<String, Object> data = new HashMap<>();

            data.put("categoryId", category.id);
            data.put("name", category.name);
            data.put("description",
                    "Truyện thuộc thể loại " + category.name + ".");
            data.put("iconUrl", "");
            data.put("dataSource", "STORYVERSE_MOCK_360");

            writer.set(
                    db.collection("categories").document(category.id),
                    data
            );
        }
    }

    /**
     * Tạo 30 tác giả mẫu.
     */
    private static void addAuthors(
            FirebaseFirestore db,
            BatchWriter writer
    ) {
        for (int i = 0; i < AUTHOR_NAMES.length; i++) {

            String authorId = String.format(
                    Locale.US, "mock_author_%03d", i + 1
            );

            Map<String, Object> data = new HashMap<>();

            data.put("authorId", authorId);
            data.put("name", AUTHOR_NAMES[i]);
            data.put("bio",
                    "Tác giả sáng tác các tác phẩm hư cấu phục vụ "
                            + "dữ liệu minh họa và kiểm thử STORYVERSE.");
            data.put("avatarUrl", "");
            data.put("dataSource", "STORYVERSE_MOCK_360");

            writer.set(
                    db.collection("authors").document(authorId),
                    data
            );
        }
    }

    /**
     * Tạo 360 truyện và các chương tương ứng.
     */
    private static void addStoriesAndChapters(
            FirebaseFirestore db,
            BatchWriter writer,
            long now
    ) {
        int storyNumber = 0;

        for (int categoryIndex = 0;
             categoryIndex < CATEGORIES.length;
             categoryIndex++) {

            CategorySeed primary = CATEGORIES[categoryIndex];

            for (int inCategory = 1;
                 inCategory <= primary.storyCount;
                 inCategory++) {

                storyNumber++;

                String storyId = String.format(
                        Locale.US, "mock_story_%03d", storyNumber
                );

                String authorId = String.format(
                        Locale.US,
                        "mock_author_%03d",
                        ((storyNumber - 1) % AUTHOR_NAMES.length) + 1
                );

                String title = buildTitle(
                        storyNumber, primary.name
                );

                CategorySeed secondary = CATEGORIES[
                        (categoryIndex + 1
                                + (inCategory % (CATEGORIES.length - 1)))
                                % CATEGORIES.length
                        ];

                Map<String, Object> story = new HashMap<>();

                story.put("storyId", storyId);
                story.put("title", title);
                story.put("description",
                        buildDescription(title, primary.name));

                // Gán ảnh bìa theo thể loại chính của truyện.
                story.put("coverUrl", getCoverUrl(primary.id));

                story.put("authorId", authorId);

                story.put("categoryIds",
                        Arrays.asList(primary.id, secondary.id));

                story.put("status", "published");
                story.put("language", "vi");
                story.put("ageRating", "13+");

                story.put("viewCount",
                        (long) (1000 + storyNumber * 137));

                story.put("favoriteCount",
                        (long) (storyNumber * 7 % 500));

                story.put("ratingAverage",
                        4.0 + (storyNumber % 10) / 10.0);

                story.put("ratingCount",
                        (long) (10 + storyNumber % 90));

                story.put("chapterCount",
                        (long) CHAPTERS_PER_STORY);

                story.put("previewChapterCount",
                        (long) CHAPTERS_PER_STORY);

                story.put("source", "STORYVERSE_MOCK");
                story.put("sourceUrl", "");
                story.put("license",
                        "Original generated demo text for academic testing");

                story.put("createdAt",
                        now - storyNumber * 60000L);

                story.put("updatedAt", now);
                story.put("dataSource", "STORYVERSE_MOCK_360");

                writer.set(
                        db.collection("stories").document(storyId),
                        story
                );

                addRelation(db, writer, storyId, primary.id);
                addRelation(db, writer, storyId, secondary.id);

                addChapters(
                        db, writer, storyId,
                        title, primary.name, now
                );
            }
        }
    }

    /**
     * Tạo liên kết giữa truyện và thể loại.
     */
    private static void addRelation(
            FirebaseFirestore db,
            BatchWriter writer,
            String storyId,
            String categoryId
    ) {
        Map<String, Object> relation = new HashMap<>();

        relation.put("storyId", storyId);
        relation.put("categoryId", categoryId);
        relation.put("dataSource", "STORYVERSE_MOCK_360");

        writer.set(
                db.collection("story_categories")
                        .document(storyId + "__" + categoryId),
                relation
        );
    }

    /**
     * Tạo 3 chương mẫu cho mỗi truyện.
     */
    private static void addChapters(
            FirebaseFirestore db,
            BatchWriter writer,
            String storyId,
            String storyTitle,
            String categoryName,
            long now
    ) {
        String[] chapterNames = {
                "Khởi đầu",
                "Biến cố",
                "Bước ngoặt"
        };

        for (int i = 1; i <= CHAPTERS_PER_STORY; i++) {

            String chapterId = storyId + "_chapter_"
                    + String.format(Locale.US, "%03d", i);

            String content = buildChapterContent(
                    storyTitle, categoryName, i
            );

            Map<String, Object> chapter = new HashMap<>();

            chapter.put("chapterId", chapterId);
            chapter.put("storyId", storyId);
            chapter.put("chapterNumber", i);
            chapter.put("title",
                    "Chương " + i + ": " + chapterNames[i - 1]);
            chapter.put("content", content);

            chapter.put("wordCount",
                    content.trim().split("\\s+").length);

            chapter.put("publishedAt", now);
            chapter.put("createdAt", now);
            chapter.put("updatedAt", now);
            chapter.put("dataSource", "STORYVERSE_MOCK_360");

            writer.set(
                    db.collection("stories")
                            .document(storyId)
                            .collection("chapters")
                            .document(chapterId),
                    chapter
            );
        }
    }

    /**
     * Lấy URL ảnh bìa GitHub theo thể loại chính.
     * URL được ghim theo commit để tránh thay đổi khi cập nhật develop.
     */
    private static String getCoverUrl(String categoryId) {

        String fileName;

        switch (categoryId) {

            case "category_ngon_tinh":
                fileName = "ngon_tinh.jpg";
                break;

            case "category_tien_hiep":
                fileName = "tien_hiep.jpg";
                break;

            case "category_kiem_hiep":
                fileName = "kiem_hiep.jpg";
                break;

            case "category_huyen_huyen":
                fileName = "ky_ao.jpg";
                break;

            case "category_trinh_tham":
                fileName = "trinh_tham.jpg";
                break;

            case "category_kinh_di":
                fileName = "kinh_di.jpg";
                break;

            case "category_do_thi":
                fileName = "do_thi.jpg";
                break;

            case "category_xuyen_khong":
                fileName = "xuyen_khong.jpg";
                break;

            case "category_trong_sinh":
                fileName = "trong_sinh.jpg";
                break;

            case "category_hoc_duong":
                fileName = "hoc_duong.jpg";
                break;

            case "category_hanh_dong":
                fileName = "hanh_dong.jpg";
                break;

            case "category_khoa_hoc_vien_tuong":
                fileName = "khoa_hoc_vien_tuong.jpg";
                break;

            case "category_hai_huoc":
                fileName = "hai_huoc.jpg";
                break;

            case "category_lich_su":
                fileName = "lich_su.jpg";
                break;

            case "category_phieu_luu":
                fileName = "phieu_luu.jpg";
                break;

            case "category_tam_ly":
                fileName = "tam_ly.jpg";
                break;

            default:
                fileName = "do_thi.jpg";
                break;
        }

        return "https://raw.githubusercontent.com/24130309-commits/StoryVerse/795625af51fdaa3696a253ea96a26ed3f1533fa4/covers/"
                + fileName;
    }

    /**
     * Tạo tên truyện mẫu.
     */
    private static String buildTitle(
            int number,
            String category
    ) {
        String prefix = TITLE_PREFIXES[
                (number - 1) % TITLE_PREFIXES.length
                ];

        String suffix = TITLE_SUFFIXES[
                ((number - 1) / TITLE_PREFIXES.length)
                        % TITLE_SUFFIXES.length
                ];

        return prefix + " " + suffix + " – " + category + " "
                + String.format(Locale.US, "%03d", number);
    }

    /**
     * Tạo mô tả truyện mẫu.
     */
    private static String buildDescription(
            String title,
            String category
    ) {
        return title
                + " là câu chuyện hư cấu thuộc thể loại "
                + category
                + ". Tác phẩm được tạo làm dữ liệu minh họa để nhóm "
                + "kiểm thử danh mục, tìm kiếm, lọc truyện và chức năng "
                + "đọc của STORYVERSE.";
    }

    /**
     * Tạo nội dung chương mẫu.
     */
    private static String buildChapterContent(
            String title,
            String category,
            int chapterNumber
    ) {
        return "Đây là nội dung chương mẫu "
                + chapterNumber
                + " của truyện “"
                + title
                + "”.\n\n"

                + "Trong thế giới "
                + category
                + ", nhân vật bắt đầu đối diện với những lựa chọn mới. "
                + "Mỗi quyết định mở ra một tình huống khác, đặt ra "
                + "thử thách và tạo động lực cho hành trình tiếp theo.\n\n"

                + "Đoạn văn này là nội dung hư cấu nguyên bản dùng để "
                + "kiểm thử hiển thị văn bản, xuống dòng, chuyển chương "
                + "và lưu nội dung đọc offline. Đây không phải toàn văn "
                + "một tác phẩm xuất bản.\n\n"

                + "Khi nhóm bổ sung nội dung truyện chính thức, có thể "
                + "thay phần văn bản mẫu này mà không cần thay đổi "
                + "cấu trúc dữ liệu.";
    }

    /**
     * Ghi marker sau khi toàn bộ dữ liệu được nạp thành công.
     */
    private static void writeMarker(
            FirebaseFirestore db,
            long now,
            Callback callback
    ) {
        Map<String, Object> marker = new HashMap<>();

        marker.put("version", VERSION);
        marker.put("seededAt", now);
        marker.put("storyCount", 360);
        marker.put("authorCount", AUTHOR_NAMES.length);
        marker.put("categoryCount", CATEGORIES.length);
        marker.put("chaptersPerStory", CHAPTERS_PER_STORY);
        marker.put("note",
                "Development mock catalog; generated original sample text.");

        db.collection("system").document(VERSION).set(marker)
                .addOnSuccessListener(unused ->
                        complete(callback, true,
                                "Đã tạo 360 truyện, 30 tác giả, "
                                        + "16 thể loại, 720 quan hệ "
                                        + "và 1.080 chương trên Firestore."))
                .addOnFailureListener(error ->
                        complete(callback, false,
                                "Dữ liệu đã ghi nhưng không tạo được marker: "
                                        + error.getMessage()
                                        + ". Có thể chạy lại an toàn vì "
                                        + "ID tài liệu là cố định."));
    }

    /**
     * Trả kết quả về nơi gọi Seeder.
     */
    private static void complete(
            Callback callback,
            boolean success,
            String message
    ) {
        if (callback != null) {
            callback.onComplete(success, message);
        }
    }

    /**
     * Cấu trúc dữ liệu thể loại.
     */
    private static final class CategorySeed {

        final String id;
        final String name;
        final int storyCount;

        CategorySeed(
                String id,
                String name,
                int storyCount
        ) {
            this.id = id;
            this.name = name;
            this.storyCount = storyCount;
        }
    }

    /**
     * Quản lý việc chia dữ liệu thành nhiều Firestore batch.
     */
    private static final class BatchWriter {

        private final FirebaseFirestore db;
        private final List<Task<Void>> commits = new ArrayList<>();

        private WriteBatch batch;
        private int writesInBatch;

        BatchWriter(FirebaseFirestore db) {
            this.db = db;
            this.batch = db.batch();
        }

        void set(
                com.google.firebase.firestore.DocumentReference ref,
                Map<String, Object> data
        ) {
            batch.set(ref, data);
            writesInBatch++;

            if (writesInBatch >= BATCH_LIMIT) {
                flush();
            }
        }

        private void flush() {

            if (writesInBatch == 0) {
                return;
            }

            commits.add(batch.commit());

            batch = db.batch();
            writesInBatch = 0;
        }

        Task<Void> commitAll() {
            flush();
            return Tasks.whenAll(commits);
        }
    }
}
