
package com.example.storyverse.data.remote.firebase;

import com.example.storyverse.data.local.entity.ChapterEntity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChapterFirestoreDataSource {

    private static final int GUEST_PREVIEW_CHAPTERS = 3;

    private final FirestoreManager firestoreManager;

    public ChapterFirestoreDataSource() {
        firestoreManager = new FirestoreManager();
    }

    // Lấy danh sách chương của một truyện.
    // Khách: tối đa 3 chương đầu.
    // Đã đăng nhập: toàn bộ chương.
    public void getChaptersByStoryId(
            String storyId,
            FirestoreManager.FirestoreCallback<List<ChapterEntity>> callback
    ) {

        if (storyId == null || storyId.trim().isEmpty()) {
            notifyFailure(callback,
                    new IllegalArgumentException("Story ID không hợp lệ"));
            return;
        }

        String collectionPath = "stories/" + storyId + "/chapters";

        Query query = firestoreManager
                .getCollection(collectionPath)
                .orderBy("chapterNumber", Query.Direction.ASCENDING);

        boolean isSignedIn =
                FirebaseAuth.getInstance().getCurrentUser() != null;

        if (!isSignedIn) {
            query = query.whereLessThanOrEqualTo(
                    "chapterNumber",
                    GUEST_PREVIEW_CHAPTERS
            );
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<ChapterEntity> chapters =
                            convertDocuments(storyId, snapshot);

                    if (callback != null) {
                        callback.onSuccess(chapters);
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error));
    }

    private List<ChapterEntity> convertDocuments(
            String storyId,
            QuerySnapshot snapshot
    ) {

        List<ChapterEntity> chapters = new ArrayList<>();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ChapterEntity chapter = convertDocument(storyId, document);

            if (chapter != null) {
                chapters.add(chapter);
            }
        }

        return chapters;
    }

    private ChapterEntity convertDocument(
            String storyId,
            DocumentSnapshot document
    ) {

        if (document == null || !document.exists()) {
            return null;
        }

        String chapterId = document.getString("chapterId");

        if (chapterId == null || chapterId.trim().isEmpty()) {
            chapterId = document.getId();
        }

        String storedStoryId = document.getString("storyId");

        if (storedStoryId == null || storedStoryId.trim().isEmpty()) {
            storedStoryId = storyId;
        }

        String title = document.getString("title");
        String content = document.getString("content");

        int chapterNumber = getInt(document, "chapterNumber");
        int wordCount = getInt(document, "wordCount");

        long publishedAt = getLong(document, "publishedAt");
        long createdAt = getLong(document, "createdAt");
        long updatedAt = getLong(document, "updatedAt");

        return new ChapterEntity(
                chapterId,
                storedStoryId,
                chapterNumber,
                title != null ? title : "",
                content != null ? content : "",
                wordCount,
                publishedAt,
                createdAt,
                updatedAt
        );
    }

    private int getInt(DocumentSnapshot document, String field) {
        Object value = document.get(field);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return 0;
    }

    private long getLong(DocumentSnapshot document, String field) {
        Object value = document.get(field);

        if (value instanceof Timestamp) {
            return ((Timestamp) value).toDate().getTime();
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return 0L;
    }

    private void notifyFailure(
            FirestoreManager.FirestoreCallback<?> callback,
            Exception error
    ) {
        if (callback != null) {
            callback.onFailure(error);
        }
    }
}
