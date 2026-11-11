
package com.example.storyverse.data.remote;

import com.example.storyverse.data.remote.firebase.FirestoreManager;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoryFirestoreDataSource {

    private static final String COLLECTION_STORIES = "stories";
    private static final String COLLECTION_AUTHORS = "authors";
    private static final String COLLECTION_CATEGORIES = "categories";
    private static final String COLLECTION_STORY_CATEGORIES = "story_categories";

    private final FirestoreManager firestoreManager;

    public StoryFirestoreDataSource() {
        firestoreManager = new FirestoreManager();
    }

    // =========================================
    // LẤY DANH SÁCH TRUYỆN ĐÃ XUẤT BẢN
    // DÀNH CHO NGƯỜI ĐỌC
    // =========================================

    public void getPublishedStories(
            FirestoreManager.FirestoreCallback<List<StoryEntity>> callback
    ) {

        firestoreManager.getCollection(COLLECTION_STORIES)
                .whereEqualTo("status", "published")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<StoryEntity> stories = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {

                        StoryEntity story = convertDocumentToEntity(document);

                        if (story != null) {
                            stories.add(story);
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(stories);
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // =========================================
    // LẤY TRUYỆN THEO ID
    // CHỈ LẤY TRUYỆN ĐÃ XUẤT BẢN
    // =========================================

    public void getStoryById(
            String storyId,
            FirestoreManager.FirestoreCallback<StoryEntity> callback
    ) {

        if (!isValidId(storyId)) {

            notifyFailure(
                    callback,
                    new IllegalArgumentException(
                            "storyId không được để trống"
                    )
            );

            return;
        }

        firestoreManager.getCollection(COLLECTION_STORIES)
                .document(storyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()
                            || !"published".equals(document.getString("status"))) {

                        if (callback != null) {
                            callback.onSuccess(null);
                        }

                        return;
                    }

                    StoryEntity story = convertDocumentToEntity(document);

                    if (callback != null) {
                        callback.onSuccess(story);
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // =========================================
    // API -> FIRESTORE
    // LƯU STORY + AUTHOR + CATEGORY + RELATION
    // =========================================

    public void saveImportedStory(
            StoryEntity story,
            AuthorEntity author,
            List<CategoryEntity> categories,
            List<StoryCategoryEntity> relations,
            FirestoreManager.FirestoreCallback<Boolean> callback
    ) {

        if (story == null || !isValidId(story.getStoryId())) {

            notifyFailure(
                    callback,
                    new IllegalArgumentException(
                            "Thông tin truyện không hợp lệ"
                    )
            );

            return;
        }

        List<CategoryEntity> safeCategories =
                categories != null ? categories : new ArrayList<>();

        List<StoryCategoryEntity> safeRelations =
                relations != null ? relations : new ArrayList<>();

        // Chỉ truy vấn quan hệ cũ khi có danh sách thể loại mới.
        if (!safeCategories.isEmpty()) {

            firestoreManager.getCollection(COLLECTION_STORY_CATEGORIES)
                    .whereEqualTo("storyId", story.getStoryId())
                    .get()
                    .addOnSuccessListener(oldRelations -> {

                        executeSaveTransaction(
                                story,
                                author,
                                safeCategories,
                                safeRelations,
                                oldRelations,
                                callback
                        );
                    })
                    .addOnFailureListener(error ->
                            notifyFailure(callback, error)
                    );

        } else {

            // Không có thể loại mới:
            // Giữ nguyên categoryIds và quan hệ cũ trên Firestore.
            executeSaveTransaction(
                    story,
                    author,
                    safeCategories,
                    safeRelations,
                    null,
                    callback
            );
        }
    }

    // =========================================
    // TRANSACTION LƯU DỮ LIỆU FIRESTORE
    // =========================================

    private void executeSaveTransaction(
            StoryEntity story,
            AuthorEntity author,
            List<CategoryEntity> categories,
            List<StoryCategoryEntity> relations,
            QuerySnapshot oldRelations,
            FirestoreManager.FirestoreCallback<Boolean> callback
    ) {

        DocumentReference storyRef =
                firestoreManager.getCollection(COLLECTION_STORIES)
                        .document(story.getStoryId());

        storyRef.getFirestore()
                .runTransaction(transaction -> {

                    DocumentSnapshot existingStory =
                            transaction.get(storyRef);

                    Map<String, Object> storyData = new LinkedHashMap<>();

                    storyData.put("storyId", story.getStoryId());

                    putIfNotEmpty(storyData, "title", story.getTitle());
                    putIfNotEmpty(storyData, "description", story.getDescription());
                    putIfNotEmpty(storyData, "coverUrl", story.getCoverUrl());
                    putIfNotEmpty(storyData, "authorId", story.getAuthorId());
                    putIfNotEmpty(storyData, "language", story.getLanguage());
                    putIfNotEmpty(storyData, "source", story.getSource());
                    putIfNotEmpty(storyData, "sourceUrl", story.getSourceUrl());
                    putIfNotEmpty(storyData, "license", story.getLicense());
                    putIfNotEmpty(storyData, "ageRating", story.getAgeRating());

                    // Chỉ thiết lập mặc định cho truyện mới.
                    // Không ghi đè trạng thái và số liệu truyện cũ.
                    if (!existingStory.exists()) {

                        storyData.put("status", "pending");
                        storyData.put("viewCount", 0L);
                        storyData.put("favoriteCount", 0L);
                        storyData.put("ratingAverage", 0.0);
                        storyData.put("ratingCount", 0L);

                        storyData.put(
                                "chapterCount",
                                story.getChapterCount()
                        );

                        long previewCount =
                                story.getPreviewChapterCount() > 0
                                        ? story.getPreviewChapterCount()
                                        : 3;

                        storyData.put(
                                "previewChapterCount",
                                previewCount
                        );

                        storyData.put(
                                "createdAt",
                                FieldValue.serverTimestamp()
                        );
                    }

                    // Tạo danh sách categoryIds không trùng.
                    Set<String> newCategoryIds = new LinkedHashSet<>();

                    for (CategoryEntity category : categories) {

                        if (category != null
                                && isValidId(category.getCategoryId())) {

                            newCategoryIds.add(category.getCategoryId());
                        }
                    }

                    if (!newCategoryIds.isEmpty()) {

                        storyData.put(
                                "categoryIds",
                                new ArrayList<>(newCategoryIds)
                        );
                    }

                    storyData.put(
                            "updatedAt",
                            FieldValue.serverTimestamp()
                    );

                    transaction.set(
                            storyRef,
                            storyData,
                            SetOptions.merge()
                    );

                    // ---------------------------------
                    // LƯU AUTHOR
                    // ---------------------------------

                    if (author != null
                            && isValidId(author.getAuthorId())) {

                        DocumentReference authorRef =
                                firestoreManager.getCollection(COLLECTION_AUTHORS)
                                        .document(author.getAuthorId());

                        Map<String, Object> authorData = new LinkedHashMap<>();

                        authorData.put("authorId", author.getAuthorId());

                        putIfNotEmpty(authorData, "name", author.getName());
                        putIfNotEmpty(authorData, "avatarUrl", author.getAvatarUrl());
                        putIfNotEmpty(authorData, "bio", author.getBio());

                        transaction.set(
                                authorRef,
                                authorData,
                                SetOptions.merge()
                        );
                    }

                    // ---------------------------------
                    // LƯU CATEGORY
                    // ---------------------------------

                    for (CategoryEntity category : categories) {

                        if (category == null
                                || !isValidId(category.getCategoryId())) {
                            continue;
                        }

                        DocumentReference categoryRef =
                                firestoreManager.getCollection(COLLECTION_CATEGORIES)
                                        .document(category.getCategoryId());

                        Map<String, Object> categoryData = new LinkedHashMap<>();

                        categoryData.put(
                                "categoryId",
                                category.getCategoryId()
                        );

                        putIfNotEmpty(categoryData, "name", category.getName());

                        putIfNotEmpty(
                                categoryData,
                                "description",
                                category.getDescription()
                        );

                        putIfNotEmpty(
                                categoryData,
                                "iconUrl",
                                category.getIconUrl()
                        );

                        transaction.set(
                                categoryRef,
                                categoryData,
                                SetOptions.merge()
                        );
                    }

                    // ---------------------------------
                    // CHUẨN HÓA QUAN HỆ STORY - CATEGORY
                    // ---------------------------------

                    Map<String, StoryCategoryEntity> expectedRelations =
                            new LinkedHashMap<>();

                    for (String categoryId : newCategoryIds) {

                        StoryCategoryEntity relation =
                                new StoryCategoryEntity();

                        relation.setStoryId(story.getStoryId());
                        relation.setCategoryId(categoryId);

                        String relationId =
                                story.getStoryId() + "__" + categoryId;

                        expectedRelations.put(relationId, relation);
                    }

                    for (StoryCategoryEntity relation : relations) {

                        if (relation == null
                                || !story.getStoryId().equals(relation.getStoryId())
                                || !newCategoryIds.contains(relation.getCategoryId())) {
                            continue;
                        }

                        String relationId =
                                relation.getStoryId()
                                        + "__"
                                        + relation.getCategoryId();

                        expectedRelations.put(relationId, relation);
                    }

                    // Xóa quan hệ cũ không còn trong danh sách mới.
                    if (oldRelations != null) {

                        for (DocumentSnapshot oldRelation
                                : oldRelations.getDocuments()) {

                            if (!expectedRelations.containsKey(oldRelation.getId())) {

                                transaction.delete(
                                        oldRelation.getReference()
                                );
                            }
                        }
                    }

                    // Lưu quan hệ mới.
                    for (Map.Entry<String, StoryCategoryEntity> entry
                            : expectedRelations.entrySet()) {

                        String relationId = entry.getKey();

                        StoryCategoryEntity relation = entry.getValue();

                        DocumentReference relationRef =
                                firestoreManager.getCollection(
                                        COLLECTION_STORY_CATEGORIES
                                ).document(relationId);

                        Map<String, Object> relationData = new LinkedHashMap<>();

                        relationData.put(
                                "storyId",
                                relation.getStoryId()
                        );

                        relationData.put(
                                "categoryId",
                                relation.getCategoryId()
                        );

                        transaction.set(
                                relationRef,
                                relationData,
                                SetOptions.merge()
                        );
                    }

                    return true;

                })
                .addOnSuccessListener(result -> {

                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // =========================================
    // TƯƠNG THÍCH VỚI CODE CŨ
    // =========================================

    public void saveImportedStory(
            StoryEntity story,
            FirestoreManager.FirestoreCallback<Boolean> callback
    ) {

        saveImportedStory(
                story,
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                callback
        );
    }

    // =========================================
    // FIRESTORE -> ROOM
    // DỮ LIỆU ĐỒNG BỘ
    // =========================================

    public interface SyncCallback {

        void onSuccess(FirestoreSyncData data);

        void onError(Exception error);
    }

    public static class FirestoreSyncData {

        public final List<StoryEntity> stories;
        public final List<AuthorEntity> authors;
        public final List<CategoryEntity> categories;
        public final List<StoryCategoryEntity> relations;

        public FirestoreSyncData(
                List<StoryEntity> stories,
                List<AuthorEntity> authors,
                List<CategoryEntity> categories,
                List<StoryCategoryEntity> relations
        ) {

            this.stories = stories;
            this.authors = authors;
            this.categories = categories;
            this.relations = relations;
        }
    }

    // =========================================
    // LẤY TẤT CẢ TRUYỆN VÀ DỮ LIỆU LIÊN QUAN
    // BAO GỒM PENDING, PUBLISHED, REJECTED...
    // =========================================

    public void getAllStoriesWithRelations(SyncCallback callback) {

        if (callback == null) {
            return;
        }

        try {

            // Không lọc status.
            // Không dùng orderBy(createdAt) để tránh bỏ sót
            // tài liệu cũ chưa có trường createdAt.

            com.google.android.gms.tasks.Task<QuerySnapshot> storiesTask =
                    firestoreManager.getCollection(COLLECTION_STORIES)
                            .get();

            com.google.android.gms.tasks.Task<QuerySnapshot> authorsTask =
                    firestoreManager.getCollection(COLLECTION_AUTHORS)
                            .get();

            com.google.android.gms.tasks.Task<QuerySnapshot> categoriesTask =
                    firestoreManager.getCollection(COLLECTION_CATEGORIES)
                            .get();

            com.google.android.gms.tasks.Task<QuerySnapshot> relationsTask =
                    firestoreManager.getCollection(COLLECTION_STORY_CATEGORIES)
                            .get();

            Tasks.whenAllSuccess(
                    storiesTask,
                    authorsTask,
                    categoriesTask,
                    relationsTask
            ).addOnSuccessListener(results -> {

                try {

                    QuerySnapshot storySnapshot =
                            (QuerySnapshot) results.get(0);

                    QuerySnapshot authorSnapshot =
                            (QuerySnapshot) results.get(1);

                    QuerySnapshot categorySnapshot =
                            (QuerySnapshot) results.get(2);

                    QuerySnapshot relationSnapshot =
                            (QuerySnapshot) results.get(3);

                    List<StoryEntity> stories = new ArrayList<>();

                    Map<String, AuthorEntity> authorMap =
                            new LinkedHashMap<>();

                    Map<String, CategoryEntity> categoryMap =
                            new LinkedHashMap<>();

                    Map<String, StoryCategoryEntity> relationMap =
                            new LinkedHashMap<>();

                    Set<String> storyIds = new LinkedHashSet<>();
                    Set<String> authorIds = new LinkedHashSet<>();
                    Set<String> categoryIds = new LinkedHashSet<>();

                    // ---------------------------------
                    // 1. LẤY TẤT CẢ TRUYỆN
                    // ---------------------------------

                    for (DocumentSnapshot document : storySnapshot.getDocuments()) {

                        StoryEntity story = convertDocumentToEntity(document);

                        if (story == null || !isValidId(story.getStoryId())) {
                            continue;
                        }

                        if (!storyIds.add(story.getStoryId())) {
                            continue;
                        }

                        stories.add(story);

                        if (isValidId(story.getAuthorId())) {
                            authorIds.add(story.getAuthorId());
                        }

                        // Lấy categoryIds từ tài liệu truyện.
                        Object rawCategoryIds = document.get("categoryIds");

                        if (rawCategoryIds instanceof List<?>) {

                            for (Object item : (List<?>) rawCategoryIds) {

                                if (!(item instanceof String)) {
                                    continue;
                                }

                                String categoryId = (String) item;

                                if (!isValidId(categoryId)) {
                                    continue;
                                }

                                categoryIds.add(categoryId);

                                addRelation(
                                        relationMap,
                                        story.getStoryId(),
                                        categoryId
                                );
                            }
                        }
                    }

                    // ---------------------------------
                    // 2. LẤY QUAN HỆ STORY - CATEGORY
                    // ---------------------------------

                    for (DocumentSnapshot document : relationSnapshot.getDocuments()) {

                        String storyId = document.getString("storyId");
                        String categoryId = document.getString("categoryId");

                        if (!isValidId(storyId)
                                || !isValidId(categoryId)
                                || !storyIds.contains(storyId)) {
                            continue;
                        }

                        categoryIds.add(categoryId);

                        addRelation(
                                relationMap,
                                storyId,
                                categoryId
                        );
                    }

                    // ---------------------------------
                    // 3. LẤY TÁC GIẢ
                    // ---------------------------------

                    for (DocumentSnapshot document : authorSnapshot.getDocuments()) {

                        String authorId = document.getId();

                        if (!authorIds.contains(authorId)) {
                            continue;
                        }

                        AuthorEntity author = new AuthorEntity();

                        author.setAuthorId(authorId);
                        author.setName(document.getString("name"));
                        author.setAvatarUrl(document.getString("avatarUrl"));
                        author.setBio(document.getString("bio"));

                        authorMap.put(authorId, author);
                    }

                    // Bổ sung tác giả bị thiếu.
                    // Các trường còn lại để null nhằm tránh ghi đè
                    // thông tin hiện có trong Room.

                    for (String authorId : authorIds) {

                        if (!authorMap.containsKey(authorId)) {

                            AuthorEntity author = new AuthorEntity();

                            author.setAuthorId(authorId);
                            author.setName(null);

                            authorMap.put(authorId, author);
                        }
                    }

                    // ---------------------------------
                    // 4. LẤY THỂ LOẠI
                    // ---------------------------------

                    for (DocumentSnapshot document : categorySnapshot.getDocuments()) {

                        String categoryId = document.getId();

                        if (!categoryIds.contains(categoryId)) {
                            continue;
                        }

                        CategoryEntity category = new CategoryEntity();

                        category.setCategoryId(categoryId);
                        category.setName(document.getString("name"));
                        category.setDescription(document.getString("description"));
                        category.setIconUrl(document.getString("iconUrl"));

                        categoryMap.put(categoryId, category);
                    }

                    // Bổ sung thể loại bị thiếu.
                    // Các trường còn lại để null nhằm tránh ghi đè
                    // thông tin hiện có trong Room.

                    for (String categoryId : categoryIds) {

                        if (!categoryMap.containsKey(categoryId)) {

                            CategoryEntity category = new CategoryEntity();

                            category.setCategoryId(categoryId);
                            category.setName(null);

                            categoryMap.put(categoryId, category);
                        }
                    }

                    // ---------------------------------
                    // 5. TRẢ KẾT QUẢ ĐỒNG BỘ
                    // ---------------------------------

                    callback.onSuccess(
                            new FirestoreSyncData(
                                    stories,
                                    new ArrayList<>(authorMap.values()),
                                    new ArrayList<>(categoryMap.values()),
                                    new ArrayList<>(relationMap.values())
                            )
                    );

                } catch (Exception error) {

                    callback.onError(error);
                }

            }).addOnFailureListener(callback::onError);

        } catch (Exception error) {

            callback.onError(error);
        }
    }

    // =========================================
    // TƯƠNG THÍCH VỚI REPOSITORY CŨ
    //
    // Tên phương thức cũ được giữ lại để không
    // làm hỏng các nơi đang gọi phương thức này.
    //
    // Lưu ý: Hiện tại phương thức này đồng bộ
    // TẤT CẢ trạng thái, không chỉ published.
    // =========================================

    public void getPublishedStoriesWithRelations(SyncCallback callback) {

        getAllStoriesWithRelations(callback);
    }

    // =========================================
    // HELPER: TẠO QUAN HỆ STORY - CATEGORY
    // =========================================

    private void addRelation(
            Map<String, StoryCategoryEntity> relationMap,
            String storyId,
            String categoryId
    ) {

        String relationId = storyId + "__" + categoryId;

        if (relationMap.containsKey(relationId)) {
            return;
        }

        StoryCategoryEntity relation = new StoryCategoryEntity();

        relation.setStoryId(storyId);
        relation.setCategoryId(categoryId);

        relationMap.put(relationId, relation);
    }

    // =========================================
    // HELPER: KIỂM TRA ID
    // =========================================

    private boolean isValidId(String id) {

        return id != null && !id.trim().isEmpty();
    }

    // =========================================
    // HELPER: THÊM STRING NẾU KHÔNG RỖNG
    // =========================================

    private void putIfNotEmpty(
            Map<String, Object> data,
            String key,
            String value
    ) {

        if (value != null && !value.trim().isEmpty()) {
            data.put(key, value.trim());
        }
    }

    // =========================================
    // HELPER: CALLBACK LỖI
    // =========================================

    private <T> void notifyFailure(
            FirestoreManager.FirestoreCallback<T> callback,
            Exception error
    ) {

        if (callback != null) {
            callback.onFailure(error);
        }
    }

    // =========================================
    // FIRESTORE DOCUMENT -> STORY ENTITY
    // =========================================

    private StoryEntity convertDocumentToEntity(DocumentSnapshot document) {

        if (document == null || !document.exists()) {
            return null;
        }

        String storyId = document.getString("storyId");

        if (!isValidId(storyId)) {
            storyId = document.getId();
        }

        StoryEntity story = new StoryEntity();

        story.setStoryId(storyId);
        story.setTitle(document.getString("title"));
        story.setDescription(document.getString("description"));
        story.setCoverUrl(document.getString("coverUrl"));
        story.setAuthorId(document.getString("authorId"));
        story.setStatus(document.getString("status"));
        story.setLanguage(document.getString("language"));
        story.setAgeRating(document.getString("ageRating"));

        story.setViewCount(getLongValue(document.get("viewCount")));
        story.setFavoriteCount(getLongValue(document.get("favoriteCount")));
        story.setRatingAverage(getDoubleValue(document.get("ratingAverage")));
        story.setRatingCount(getLongValue(document.get("ratingCount")));
        story.setChapterCount(getLongValue(document.get("chapterCount")));
        story.setPreviewChapterCount(getLongValue(document.get("previewChapterCount")));

        story.setSource(document.getString("source"));
        story.setSourceUrl(document.getString("sourceUrl"));
        story.setLicense(document.getString("license"));

        story.setCreatedAt(getTimestampValue(document.get("createdAt")));
        story.setUpdatedAt(getTimestampValue(document.get("updatedAt")));

        return story;
    }

    // =========================================
    // HELPER: CHUYỂN SANG LONG
    // =========================================

    private long getLongValue(Object value) {

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return 0;
    }

    // =========================================
    // HELPER: CHUYỂN SANG DOUBLE
    // =========================================

    private double getDoubleValue(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return 0;
    }

    // =========================================
    // HELPER: CHUYỂN TIMESTAMP
    // =========================================

    private long getTimestampValue(Object value) {

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return 0;
    }
}
