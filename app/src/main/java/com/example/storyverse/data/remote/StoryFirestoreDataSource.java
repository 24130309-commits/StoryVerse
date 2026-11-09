
package com.example.storyverse.data.remote;

import com.example.storyverse.data.remote.firebase.FirestoreManager;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class StoryFirestoreDataSource {

    private final FirestoreManager firestoreManager;

    public StoryFirestoreDataSource() {
        firestoreManager = new FirestoreManager();
    }

    // Lấy danh sách truyện đã xuất bản từ Firestore
    public void getPublishedStories(
            FirestoreManager.FirestoreCallback<List<StoryEntity>> callback
    ) {
        firestoreManager.getCollection("stories")
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

                    callback.onSuccess(stories);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Lấy thông tin một truyện theo ID
    // Chỉ trả về truyện đã xuất bản
    public void getStoryById(
            String storyId,
            FirestoreManager.FirestoreCallback<StoryEntity> callback
    ) {
        if (storyId == null || storyId.trim().isEmpty()) {
            callback.onFailure(
                    new IllegalArgumentException("storyId không được để trống")
            );
            return;
        }

        firestoreManager.getCollection("stories")
                .document(storyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onSuccess(null);
                        return;
                    }

                    String status = document.getString("status");

                    if (!"published".equals(status)) {
                        callback.onSuccess(null);
                        return;
                    }

                    StoryEntity story = convertDocumentToEntity(document);
                    callback.onSuccess(story);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Chuyển dữ liệu Firestore thành StoryEntity
    private StoryEntity convertDocumentToEntity(DocumentSnapshot document) {

        if (document == null || !document.exists()) {
            return null;
        }

        String storyId = document.getString("storyId");

        if (storyId == null || storyId.trim().isEmpty()) {
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
        story.setPreviewChapterCount(
                getLongValue(document.get("previewChapterCount"))
        );

        story.setSource(document.getString("source"));
        story.setSourceUrl(document.getString("sourceUrl"));
        story.setLicense(document.getString("license"));

        story.setCreatedAt(getTimestampValue(document.get("createdAt")));
        story.setUpdatedAt(getTimestampValue(document.get("updatedAt")));

        return story;
    }

    private long getLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    private double getDoubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

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
