
package com.example.storyverse.data.remote.firebase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class StoryVerseOldDataCleaner {

    private static final String DATA_SOURCE = "STORYVERSE_MOCK_360";
    private static final int BATCH_LIMIT = 400;

    private static final Set<String> NEW_CATEGORIES =
            new HashSet<>(Arrays.asList(
                    "category_ngon_tinh",
                    "category_tien_hiep",
                    "category_kiem_hiep",
                    "category_huyen_huyen",
                    "category_trinh_tham",
                    "category_kinh_di",
                    "category_do_thi",
                    "category_xuyen_khong",
                    "category_trong_sinh",
                    "category_hoc_duong",
                    "category_hanh_dong",
                    "category_khoa_hoc_vien_tuong",
                    "category_hai_huoc",
                    "category_lich_su",
                    "category_phieu_luu",
                    "category_tam_ly"
            ));

    private StoryVerseOldDataCleaner() {
    }

    public interface Callback {
        void onComplete(boolean success, String message);
    }

    public static void cleanOldData(Callback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        deleteExcept(db, "story_categories", document -> {
            String storyId = document.getString("storyId");
            String categoryId = document.getString("categoryId");

            return DATA_SOURCE.equals(document.getString("dataSource"))
                    && isNewStoryId(storyId)
                    && NEW_CATEGORIES.contains(categoryId);
        })
                .continueWithTask(task -> {
                    checkTask(task);
                    return deleteExcept(db, "authors", document ->
                            isNewAuthorId(document.getId()));
                })
                .continueWithTask(task -> {
                    checkTask(task);
                    return deleteExcept(db, "categories", document ->
                            NEW_CATEGORIES.contains(document.getId()));
                })
                .continueWithTask(task -> {
                    checkTask(task);
                    return cleanOldStoriesAndChapters(db);
                })
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onComplete(
                                true,
                                "Đã dọn dữ liệu cũ. Giữ lại bộ STORYVERSE_MOCK_360."
                        );
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onComplete(
                                false,
                                "Dọn dữ liệu thất bại: " + error.getMessage()
                        );
                    }
                });
    }

    private static Task<Void> cleanOldStoriesAndChapters(
            FirebaseFirestore db
    ) {
        return db.collection("stories").get()
                .continueWithTask(task -> {

                    checkTask(task);

                    QuerySnapshot snapshot = task.getResult();

                    List<DocumentReference> oldStories =
                            new ArrayList<>();

                    List<Task<Void>> chapterTasks =
                            new ArrayList<>();

                    for (DocumentSnapshot document : snapshot) {

                        if (isNewStoryId(document.getId())) {
                            continue;
                        }

                        oldStories.add(document.getReference());

                        Task<Void> chapterTask = document.getReference()
                                .collection("chapters")
                                .get()
                                .continueWithTask(chapterResult -> {

                                    checkTask(chapterResult);

                                    List<DocumentReference> oldChapters =
                                            new ArrayList<>();

                                    for (DocumentSnapshot chapter :
                                            chapterResult.getResult()) {
                                        oldChapters.add(
                                                chapter.getReference()
                                        );
                                    }

                                    return deleteDocuments(
                                            db,
                                            oldChapters
                                    );
                                });

                        chapterTasks.add(chapterTask);
                    }

                    return Tasks.whenAll(chapterTasks)
                            .continueWithTask(chapterResult -> {

                                checkTask(chapterResult);

                                return deleteDocuments(
                                        db,
                                        oldStories
                                );
                            });
                });
    }

    private interface KeepRule {
        boolean keep(DocumentSnapshot document);
    }

    private static Task<Void> deleteExcept(
            FirebaseFirestore db,
            String collectionName,
            KeepRule keepRule
    ) {
        return db.collection(collectionName).get()
                .continueWithTask(task -> {

                    checkTask(task);

                    List<DocumentReference> deleteList =
                            new ArrayList<>();

                    for (DocumentSnapshot document : task.getResult()) {

                        if (!keepRule.keep(document)) {
                            deleteList.add(document.getReference());
                        }
                    }

                    return deleteDocuments(db, deleteList);
                });
    }

    private static Task<Void> deleteDocuments(
            FirebaseFirestore db,
            List<DocumentReference> documents
    ) {

        Task<Void> chain = Tasks.forResult(null);

        for (int start = 0;
             start < documents.size();
             start += BATCH_LIMIT) {

            int end = Math.min(
                    start + BATCH_LIMIT,
                    documents.size()
            );

            List<DocumentReference> batchDocuments =
                    new ArrayList<>(
                            documents.subList(start, end)
                    );

            chain = chain.continueWithTask(previous -> {

                checkTask(previous);

                WriteBatch batch = db.batch();

                for (DocumentReference document : batchDocuments) {
                    batch.delete(document);
                }

                return batch.commit();
            });
        }

        return chain;
    }

    private static boolean isNewStoryId(String id) {

        if (id == null || !id.startsWith("mock_story_")) {
            return false;
        }

        try {
            int number = Integer.parseInt(
                    id.substring("mock_story_".length())
            );

            return number >= 1 && number <= 360
                    && id.equals(String.format(
                    java.util.Locale.US,
                    "mock_story_%03d",
                    number
            ));

        } catch (NumberFormatException error) {
            return false;
        }
    }

    private static boolean isNewAuthorId(String id) {

        if (id == null || !id.startsWith("mock_author_")) {
            return false;
        }

        try {
            int number = Integer.parseInt(
                    id.substring("mock_author_".length())
            );

            return number >= 1 && number <= 30
                    && id.equals(String.format(
                    java.util.Locale.US,
                    "mock_author_%03d",
                    number
            ));

        } catch (NumberFormatException error) {
            return false;
        }
    }

    private static <T> void checkTask(Task<T> task)
            throws Exception {

        if (!task.isSuccessful()) {
            Exception error = task.getException();

            if (error != null) {
                throw error;
            }

            throw new Exception("Firestore operation failed.");
        }
    }
}
