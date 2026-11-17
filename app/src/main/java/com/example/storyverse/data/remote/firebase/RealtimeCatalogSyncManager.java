
package com.example.storyverse.data.remote.firebase;

import android.content.Context;
import android.util.Log;

import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Lắng nghe dữ liệu Cloud Firestore và cập nhật Room.
 *
 * Đồng bộ:
 * - Authors
 * - Categories
 * - Stories: tất cả trạng thái
 * - Story_categories: chỉ các quan hệ hợp lệ
 *
 * Không tự động xóa dữ liệu Room khi document bị xóa trên Firestore.
 */
public class RealtimeCatalogSyncManager {

    private static final String TAG = "RealtimeCatalogSync";

    private final StoryVerseDatabase database;
    private final FirebaseFirestore firestore;

    private ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private ListenerRegistration authorsListener;
    private ListenerRegistration categoriesListener;
    private ListenerRegistration storiesListener;
    private ListenerRegistration relationsListener;

    private List<AuthorEntity> latestAuthors = new ArrayList<>();
    private List<CategoryEntity> latestCategories = new ArrayList<>();
    private List<StoryEntity> latestStories = new ArrayList<>();
    private List<StoryCategoryEntity> latestRelations = new ArrayList<>();

    private boolean authorsLoaded;
    private boolean categoriesLoaded;
    private boolean storiesLoaded;
    private boolean relationsLoaded;

    private boolean started;
    private long syncGeneration = 0;

    public RealtimeCatalogSyncManager(Context context) {

        database = StoryVerseDatabase.getInstance(
                context.getApplicationContext()
        );

        firestore = FirebaseFirestore.getInstance();
    }

    // =========================================
    // BẮT ĐẦU LẮNG NGHE FIRESTORE
    // =========================================

    public synchronized void start() {

        if (started) {
            return;
        }

        // Tạo lại Executor nếu đã shutdown trước đó.
        if (databaseExecutor == null
                || databaseExecutor.isShutdown()
                || databaseExecutor.isTerminated()) {

            databaseExecutor = Executors.newSingleThreadExecutor();
        }

        started = true;
        syncGeneration++;

        final long generation = syncGeneration;

        resetSnapshotState();

        // -------------------------------------
        // 1. AUTHORS
        // -------------------------------------

        authorsListener = firestore.collection("authors")
                .addSnapshotListener((snapshot, error) -> {

                    if (!isCurrentGeneration(generation)) {
                        return;
                    }

                    if (error != null) {
                        logError("Lắng nghe authors thất bại", error);
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<AuthorEntity> result = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String authorId = doc.getId();

                        if (!isNotEmpty(authorId)) {
                            continue;
                        }

                        AuthorEntity item = new AuthorEntity();

                        // Document ID là ID chuẩn.
                        item.setAuthorId(authorId);

                        item.setName(doc.getString("name"));
                        item.setAvatarUrl(doc.getString("avatarUrl"));
                        item.setBio(doc.getString("bio"));

                        result.add(item);
                    }

                    synchronized (this) {

                        if (!started || syncGeneration != generation) {
                            return;
                        }

                        latestAuthors = result;
                        authorsLoaded = true;
                    }

                    persistWhenReady(generation);
                });

        // -------------------------------------
        // 2. CATEGORIES
        // -------------------------------------

        categoriesListener = firestore.collection("categories")
                .addSnapshotListener((snapshot, error) -> {

                    if (!isCurrentGeneration(generation)) {
                        return;
                    }

                    if (error != null) {
                        logError("Lắng nghe categories thất bại", error);
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<CategoryEntity> result = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String categoryId = doc.getId();

                        if (!isNotEmpty(categoryId)) {
                            continue;
                        }

                        CategoryEntity item = new CategoryEntity();

                        // Document ID là ID chuẩn.
                        item.setCategoryId(categoryId);

                        item.setName(doc.getString("name"));
                        item.setDescription(doc.getString("description"));
                        item.setIconUrl(doc.getString("iconUrl"));

                        result.add(item);
                    }

                    synchronized (this) {

                        if (!started || syncGeneration != generation) {
                            return;
                        }

                        latestCategories = result;
                        categoriesLoaded = true;
                    }

                    persistWhenReady(generation);
                });

        // -------------------------------------
        // 3. STORIES
        // -------------------------------------
        // Lắng nghe tất cả trạng thái.
        // Việc lọc published được thực hiện ở DAO/Repository
        // phục vụ màn hình hiển thị truyện.

        storiesListener = firestore.collection("stories")
                .addSnapshotListener((snapshot, error) -> {

                    if (!isCurrentGeneration(generation)) {
                        return;
                    }

                    if (error != null) {
                        logError("Lắng nghe stories thất bại", error);
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<StoryEntity> result = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        StoryEntity item = mapStory(doc);

                        if (item != null
                                && isNotEmpty(item.getStoryId())) {

                            result.add(item);
                        }
                    }

                    synchronized (this) {

                        if (!started || syncGeneration != generation) {
                            return;
                        }

                        latestStories = result;
                        storiesLoaded = true;
                    }

                    persistWhenReady(generation);
                });

        // -------------------------------------
        // 4. STORY_CATEGORIES
        // -------------------------------------

        relationsListener = firestore.collection("story_categories")
                .addSnapshotListener((snapshot, error) -> {

                    if (!isCurrentGeneration(generation)) {
                        return;
                    }

                    if (error != null) {
                        logError(
                                "Lắng nghe story_categories thất bại",
                                error
                        );
                        return;
                    }

                    if (snapshot == null) {
                        return;
                    }

                    List<StoryCategoryEntity> result = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {

                        String storyId = readString(
                                doc,
                                "storyId",
                                null
                        );

                        String categoryId = readString(
                                doc,
                                "categoryId",
                                null
                        );

                        if (!isNotEmpty(storyId)
                                || !isNotEmpty(categoryId)) {
                            continue;
                        }

                        StoryCategoryEntity relation =
                                new StoryCategoryEntity();

                        relation.setStoryId(storyId);
                        relation.setCategoryId(categoryId);

                        result.add(relation);
                    }

                    synchronized (this) {

                        if (!started || syncGeneration != generation) {
                            return;
                        }

                        latestRelations = result;
                        relationsLoaded = true;
                    }

                    persistWhenReady(generation);
                });
    }

    // =========================================
    // KIỂM TRA PHIÊN ĐỒNG BỘ
    // =========================================

    private synchronized boolean isCurrentGeneration(long generation) {

        return started && syncGeneration == generation;
    }

    private void resetSnapshotState() {

        latestAuthors = new ArrayList<>();
        latestCategories = new ArrayList<>();
        latestStories = new ArrayList<>();
        latestRelations = new ArrayList<>();

        authorsLoaded = false;
        categoriesLoaded = false;
        storiesLoaded = false;
        relationsLoaded = false;
    }

    // =========================================
    // ĐỒNG BỘ DỮ LIỆU VÀO ROOM
    // =========================================

    private void persistWhenReady(long generation) {

        final List<AuthorEntity> authors;
        final List<CategoryEntity> categories;
        final List<StoryEntity> stories;
        final List<StoryCategoryEntity> relations;

        synchronized (this) {

            if (!started || syncGeneration != generation) {
                return;
            }

            // Chờ đủ 4 snapshot đầu tiên.
            if (!authorsLoaded
                    || !categoriesLoaded
                    || !storiesLoaded
                    || !relationsLoaded) {
                return;
            }

            authors = new ArrayList<>(latestAuthors);
            categories = new ArrayList<>(latestCategories);
            stories = new ArrayList<>(latestStories);
            relations = new ArrayList<>(latestRelations);
        }

        try {

            databaseExecutor.execute(() -> {

                if (!isCurrentGeneration(generation)) {
                    return;
                }

                try {

                    // ---------------------------------
                    // 1. TẬP HỢP ID TRUYỆN HỢP LỆ
                    // ---------------------------------

                    Set<String> validStoryIds = new HashSet<>();

                    for (StoryEntity story : stories) {

                        if (story != null
                                && isNotEmpty(story.getStoryId())) {

                            validStoryIds.add(story.getStoryId());
                        }
                    }

                    // ---------------------------------
                    // 2. TẬP HỢP ID THỂ LOẠI HỢP LỆ
                    // ---------------------------------

                    Set<String> validCategoryIds = new HashSet<>();

                    for (CategoryEntity category : categories) {

                        if (category != null
                                && isNotEmpty(category.getCategoryId())) {

                            validCategoryIds.add(
                                    category.getCategoryId()
                            );
                        }
                    }

                    // ---------------------------------
                    // 3. KIỂM TRA QUAN HỆ
                    // ---------------------------------

                    List<StoryCategoryEntity> validRelations =
                            new ArrayList<>();

                    Set<String> uniqueRelations = new HashSet<>();

                    int skippedRelations = 0;

                    for (StoryCategoryEntity relation : relations) {

                        if (relation == null) {
                            skippedRelations++;
                            continue;
                        }

                        String storyId = relation.getStoryId();
                        String categoryId = relation.getCategoryId();

                        boolean storyExists =
                                isNotEmpty(storyId)
                                        && validStoryIds.contains(storyId);

                        boolean categoryExists =
                                isNotEmpty(categoryId)
                                        && validCategoryIds.contains(categoryId);

                        if (!storyExists || !categoryExists) {

                            skippedRelations++;

                            Log.w(TAG,
                                    "Bỏ qua quan hệ không hợp lệ: "
                                            + "storyId=" + storyId
                                            + ", categoryId=" + categoryId
                                            + ", storyExists=" + storyExists
                                            + ", categoryExists=" + categoryExists
                            );

                            continue;
                        }

                        // Tránh chèn quan hệ trùng lặp.
                        String relationKey = storyId + "::" + categoryId;

                        if (uniqueRelations.add(relationKey)) {
                            validRelations.add(relation);
                        }
                    }

                    // ---------------------------------
                    // 4. KIỂM TRA PHIÊN TRƯỚC KHI GHI
                    // ---------------------------------

                    if (!isCurrentGeneration(generation)) {
                        return;
                    }

                    // ---------------------------------
                    // 5. GHI ROOM THEO THỨ TỰ
                    // ---------------------------------
                    // Author, Category, Story trước.
                    // StoryCategory sau.
                    //
                    // Tất cả nằm trong một transaction.

                    database.runInTransaction(() -> {

                        if (!authors.isEmpty()) {
                            database.authorDao().insertAuthors(authors);
                        }

                        if (!categories.isEmpty()) {
                            database.categoryDao()
                                    .insertCategories(categories);
                        }

                        if (!stories.isEmpty()) {
                            database.storyDao().insertStories(stories);
                        }

                        if (!validRelations.isEmpty()) {
                            database.storyCategoryDao()
                                    .insertStoryCategories(validRelations);
                        }
                    });

                    Log.d(TAG,
                            "Đồng bộ Room thành công: "
                                    + stories.size() + " truyện, "
                                    + authors.size() + " tác giả, "
                                    + categories.size() + " thể loại, "
                                    + validRelations.size() + " quan hệ hợp lệ. "
                                    + "Đã bỏ qua "
                                    + skippedRelations
                                    + " quan hệ không hợp lệ."
                    );

                } catch (Exception e) {

                    Log.e(TAG,
                            "Không thể cập nhật dữ liệu Firestore vào Room",
                            e
                    );
                }
            });

        } catch (RejectedExecutionException e) {

            Log.e(TAG,
                    "Không thể đưa tác vụ đồng bộ vào hàng đợi",
                    e
            );
        }
    }

    // =========================================
    // FIRESTORE DOCUMENT -> STORY ENTITY
    // =========================================

    private StoryEntity mapStory(DocumentSnapshot doc) {

        if (doc == null || !doc.exists()) {
            return null;
        }

        String storyId = doc.getId();

        if (!isNotEmpty(storyId)) {
            return null;
        }

        StoryEntity item = new StoryEntity();

        // Document ID là ID chuẩn của truyện.
        item.setStoryId(storyId);

        item.setTitle(doc.getString("title"));
        item.setDescription(doc.getString("description"));
        item.setCoverUrl(doc.getString("coverUrl"));
        item.setAuthorId(doc.getString("authorId"));
        item.setStatus(doc.getString("status"));
        item.setLanguage(doc.getString("language"));
        item.setAgeRating(doc.getString("ageRating"));

        item.setViewCount(readLong(doc.get("viewCount")));
        item.setFavoriteCount(readLong(doc.get("favoriteCount")));
        item.setRatingAverage(readDouble(doc.get("ratingAverage")));
        item.setRatingCount(readLong(doc.get("ratingCount")));
        item.setChapterCount(readLong(doc.get("chapterCount")));

        item.setPreviewChapterCount(
                readLong(doc.get("previewChapterCount"))
        );

        item.setSource(doc.getString("source"));
        item.setSourceUrl(doc.getString("sourceUrl"));
        item.setLicense(doc.getString("license"));

        item.setCreatedAt(readLong(doc.get("createdAt")));
        item.setUpdatedAt(readLong(doc.get("updatedAt")));

        return item;
    }

    // =========================================
    // HELPER: ĐỌC STRING
    // =========================================

    private static String readString(
            DocumentSnapshot doc,
            String field,
            String fallback
    ) {

        String value = doc.getString(field);

        return isNotEmpty(value) ? value : fallback;
    }

    // =========================================
    // HELPER: ĐỌC LONG
    // =========================================

    private static long readLong(Object value) {

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }

        return 0L;
    }

    // =========================================
    // HELPER: ĐỌC DOUBLE
    // =========================================

    private static double readDouble(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return 0.0;
    }

    // =========================================
    // HELPER: KIỂM TRA STRING
    // =========================================

    private static boolean isNotEmpty(String value) {

        return value != null && !value.trim().isEmpty();
    }

    // =========================================
    // HELPER: GHI LOG LỖI
    // =========================================

    private static void logError(
            String message,
            Exception error
    ) {

        Log.e(TAG, message, error);
    }

    // =========================================
    // DỪNG LẮNG NGHE
    // =========================================

    public synchronized void stop() {

        // Vô hiệu hóa các callback và tác vụ thuộc phiên cũ.
        started = false;
        syncGeneration++;

        removeListener(authorsListener);
        removeListener(categoriesListener);
        removeListener(storiesListener);
        removeListener(relationsListener);

        authorsListener = null;
        categoriesListener = null;
        storiesListener = null;
        relationsListener = null;

        resetSnapshotState();
    }

    private void removeListener(ListenerRegistration listener) {

        if (listener != null) {
            listener.remove();
        }
    }

    // =========================================
    // ĐÓNG MANAGER
    // =========================================

    public synchronized void shutdown() {

        stop();

        if (databaseExecutor != null
                && !databaseExecutor.isShutdown()) {

            databaseExecutor.shutdown();
        }
    }
}
