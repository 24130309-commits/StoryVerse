package com.example.storyverse.data.remote.firebase;

import com.example.storyverse.data.local.entity.ChapterEntity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChapterFirestoreDataSource {

    private static final int GUEST_PREVIEW_CHAPTERS = 3;
    // Giữ document Firestore dưới giới hạn 1 MiB, chừa chỗ cho metadata.
    private static final int FIRESTORE_INLINE_CONTENT_LIMIT = 700 * 1024;
    private static final long MAX_CHAPTER_DOWNLOAD_BYTES = 25L * 1024L * 1024L;
    private static final String STORAGE_FOLDER = "chapter_contents";

    private final FirestoreManager firestoreManager;

    public ChapterFirestoreDataSource() {
        firestoreManager = new FirestoreManager();
    }

    // =========================================
    // FIRESTORE: LƯU MỘT CHƯƠNG
    // Chương ngắn lưu inline; chương dài lưu Firebase Storage.
    // =========================================

    public void saveChapter(
            ChapterEntity chapter,
            FirestoreManager.FirestoreCallback<Void> callback
    ) {
        if (chapter == null
                || !isValidId(chapter.getChapterId())
                || !isValidId(chapter.getStoryId())
                || chapter.getChapterNumber() < 1
                || !isValidId(chapter.getTitle())
                || !isValidId(chapter.getContent())) {
            notifyFailure(callback, new IllegalArgumentException("Thông tin chương không hợp lệ"));
            return;
        }

        byte[] contentBytes = chapter.getContent().getBytes(StandardCharsets.UTF_8);
        if (contentBytes.length <= FIRESTORE_INLINE_CONTENT_LIMIT) {
            writeChapterDocument(chapter, chapter.getContent(), null, callback);
            return;
        }

        String storagePath = STORAGE_FOLDER + "/" + chapter.getChapterId() + ".txt";
        FirebaseStorage.getInstance()
                .getReference()
                .child(storagePath)
                .putBytes(contentBytes)
                .addOnSuccessListener(taskSnapshot -> {
                    chapter.setContentStoragePath(storagePath);
                    writeChapterDocument(chapter, "", storagePath, callback);
                })
                .addOnFailureListener(error -> notifyFailure(callback, error));
    }

    private void writeChapterDocument(
            ChapterEntity chapter,
            String inlineContent,
            String storagePath,
            FirestoreManager.FirestoreCallback<Void> callback
    ) {
        Map<String, Object> chapterData = new HashMap<>();
        chapterData.put("chapterId", chapter.getChapterId());
        chapterData.put("storyId", chapter.getStoryId());
        chapterData.put("chapterNumber", chapter.getChapterNumber());
        chapterData.put("title", chapter.getTitle());
        chapterData.put("content", inlineContent == null ? "" : inlineContent);
        chapterData.put("wordCount", chapter.getWordCount());
        chapterData.put("publishedAt", chapter.getPublishedAt());
        chapterData.put("createdAt", chapter.getCreatedAt());
        chapterData.put("updatedAt", chapter.getUpdatedAt());
        if (storagePath != null && !storagePath.trim().isEmpty()) {
            chapterData.put("contentStoragePath", storagePath);
        }

        firestoreManager
                .getCollection("stories/" + chapter.getStoryId() + "/chapters")
                .document(chapter.getChapterId())
                .set(chapterData)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(error -> notifyFailure(callback, error));
    }

    // =========================================
    // LẤY DANH SÁCH CHƯƠNG
    // Khách: tối đa 3 chương đầu.
    // Đăng nhập: toàn bộ chương.
    // Nội dung nằm trong Storage sẽ được tải trước khi trả về Reader.
    // =========================================

    public void getChaptersByStoryId(
            String storyId,
            FirestoreManager.FirestoreCallback<List<ChapterEntity>> callback
    ) {
        if (!isValidId(storyId)) {
            notifyFailure(callback, new IllegalArgumentException("Story ID không hợp lệ"));
            return;
        }

        String validStoryId = storyId.trim();
        Query query = firestoreManager
                .getCollection("stories/" + validStoryId + "/chapters")
                .orderBy("chapterNumber", Query.Direction.ASCENDING);

        boolean isSignedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (!isSignedIn) {
            query = query.whereLessThanOrEqualTo("chapterNumber", GUEST_PREVIEW_CHAPTERS);
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<ChapterEntity> chapters = convertDocuments(validStoryId, snapshot);
                    loadStoredContents(chapters, callback);
                })
                .addOnFailureListener(error -> notifyFailure(callback, error));
    }

    private void loadStoredContents(
            List<ChapterEntity> chapters,
            FirestoreManager.FirestoreCallback<List<ChapterEntity>> callback
    ) {
        List<Task<byte[]>> downloadTasks = new ArrayList<>();
        List<ChapterEntity> chaptersNeedingDownload = new ArrayList<>();

        for (ChapterEntity chapter : chapters) {
            String path = chapter.getContentStoragePath();
            if (chapter.getContent() != null && !chapter.getContent().isEmpty()) continue;
            if (!isValidId(path)) continue;

            downloadTasks.add(
                    FirebaseStorage.getInstance()
                            .getReference()
                            .child(path)
                            .getBytes(MAX_CHAPTER_DOWNLOAD_BYTES)
            );
            chaptersNeedingDownload.add(chapter);
        }

        if (downloadTasks.isEmpty()) {
            if (callback != null) callback.onSuccess(chapters);
            return;
        }

        Tasks.whenAllSuccess(downloadTasks)
                .addOnSuccessListener(results -> {
                    try {
                        for (int i = 0; i < results.size(); i++) {
                            byte[] bytes = (byte[]) results.get(i);
                            chaptersNeedingDownload.get(i).setContent(
                                    new String(bytes, StandardCharsets.UTF_8)
                            );
                        }
                        if (callback != null) callback.onSuccess(chapters);
                    } catch (Exception error) {
                        notifyFailure(callback, error);
                    }
                })
                .addOnFailureListener(error -> notifyFailure(callback, error));
    }

    private List<ChapterEntity> convertDocuments(
            String storyId,
            QuerySnapshot snapshot
    ) {
        List<ChapterEntity> chapters = new ArrayList<>();
        if (snapshot == null) return chapters;

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            ChapterEntity chapter = convertDocument(storyId, document);
            if (chapter != null) chapters.add(chapter);
        }
        return chapters;
    }

    private ChapterEntity convertDocument(
            String storyId,
            DocumentSnapshot document
    ) {
        if (document == null || !document.exists()) return null;

        String storedStoryId = document.getString("storyId");
        if (isValidId(storedStoryId) && !storyId.equals(storedStoryId.trim())) return null;

        int chapterNumber = getInt(document, "chapterNumber");
        if (chapterNumber < 1) return null;

        String chapterId = document.getString("chapterId");
        if (!isValidId(chapterId)) chapterId = document.getId();

        ChapterEntity chapter = new ChapterEntity(
                chapterId,
                storyId,
                chapterNumber,
                defaultString(document.getString("title")),
                defaultString(document.getString("content")),
                Math.max(getInt(document, "wordCount"), 0),
                getLong(document, "publishedAt"),
                getLong(document, "createdAt"),
                getLong(document, "updatedAt")
        );
        chapter.setContentStoragePath(document.getString("contentStoragePath"));
        return chapter;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private int getInt(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private long getLong(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    private void notifyFailure(
            FirestoreManager.FirestoreCallback<?> callback,
            Exception error
    ) {
        if (callback != null) callback.onFailure(error);
    }
}
