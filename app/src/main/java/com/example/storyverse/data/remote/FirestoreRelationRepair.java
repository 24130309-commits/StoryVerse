
package com.example.storyverse.data.remote;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class FirestoreRelationRepair {

    private static final String TAG = "RelationRepair";

    // Giữ mỗi batch dưới giới hạn 500 thao tác của Firestore.
    private static final int BATCH_LIMIT = 450;

    // Dấu phân cách thực tế trong Document ID.
    private static final String RELATION_SEPARATOR = "_category_";

    private final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public void repairRelations() {

        // Bước 1: Đọc danh sách truyện.
        db.collection("stories").get()
                .addOnSuccessListener(storiesSnapshot -> {

                    Set<String> storyIds = new HashSet<>();

                    for (DocumentSnapshot doc : storiesSnapshot) {
                        storyIds.add(doc.getId());
                    }

                    // Bước 2: Đọc danh sách thể loại.
                    db.collection("categories").get()
                            .addOnSuccessListener(categoriesSnapshot -> {

                                Set<String> categoryIds = new HashSet<>();

                                for (DocumentSnapshot doc : categoriesSnapshot) {
                                    categoryIds.add(doc.getId());
                                }

                                // Bước 3: Đọc danh sách quan hệ.
                                db.collection("story_categories").get()
                                        .addOnSuccessListener(relationsSnapshot -> {

                                            List<WriteBatch> batches =
                                                    new ArrayList<>();

                                            WriteBatch batch = db.batch();

                                            int batchOperations = 0;
                                            int updated = 0;
                                            int skipped = 0;

                                            for (DocumentSnapshot doc : relationsSnapshot) {

                                                String documentId = doc.getId();

                                                /*
                                                 * Định dạng Document ID thực tế:
                                                 * storyId_category_categoryIdSuffix
                                                 *
                                                 * Ví dụ:
                                                 * mock_story_001_category_kiem_hiep
                                                 *
                                                 * Kết quả:
                                                 * storyId = mock_story_001
                                                 * categoryId = category_kiem_hiep
                                                 */

                                                int separator =
                                                        documentId.lastIndexOf(
                                                                RELATION_SEPARATOR
                                                        );

                                                if (separator <= 0) {
                                                    Log.w(TAG,
                                                            "Bỏ qua document sai định dạng: "
                                                                    + documentId);

                                                    skipped++;
                                                    continue;
                                                }

                                                String storyId =
                                                        documentId.substring(0, separator);

                                                String categoryId =
                                                        documentId.substring(separator + 1);

                                                boolean storyExists =
                                                        storyIds.contains(storyId);

                                                boolean categoryExists =
                                                        categoryIds.contains(categoryId);

                                                if (!storyExists || !categoryExists) {

                                                    Log.w(TAG,
                                                            "Bỏ qua quan hệ không hợp lệ: "
                                                                    + documentId
                                                                    + " | storyExists="
                                                                    + storyExists
                                                                    + " | categoryExists="
                                                                    + categoryExists);

                                                    skipped++;
                                                    continue;
                                                }

                                                // Kiểm tra dữ liệu hiện tại.
                                                String oldStoryId =
                                                        doc.getString("storyId");

                                                String oldCategoryId =
                                                        doc.getString("categoryId");

                                                // Không cập nhật nếu dữ liệu đã chính xác.
                                                if (storyId.equals(oldStoryId)
                                                        && categoryId.equals(oldCategoryId)) {
                                                    continue;
                                                }

                                                Map<String, Object> updates =
                                                        new HashMap<>();

                                                updates.put("storyId", storyId);
                                                updates.put("categoryId", categoryId);

                                                batch.update(
                                                        doc.getReference(),
                                                        updates
                                                );

                                                batchOperations++;
                                                updated++;

                                                // Đủ 450 thao tác thì lưu batch hiện tại.
                                                if (batchOperations >= BATCH_LIMIT) {

                                                    batches.add(batch);

                                                    batch = db.batch();
                                                    batchOperations = 0;
                                                }
                                            }

                                            // Thêm batch cuối nếu còn thao tác.
                                            if (batchOperations > 0) {
                                                batches.add(batch);
                                            }

                                            Log.d(TAG,
                                                    "Tổng quan hệ đã đọc: "
                                                            + relationsSnapshot.size());

                                            Log.d(TAG,
                                                    "Số quan hệ cần cập nhật: "
                                                            + updated);

                                            Log.d(TAG,
                                                    "Số quan hệ bỏ qua: "
                                                            + skipped);

                                            if (batches.isEmpty()) {

                                                Log.d(TAG,
                                                        "Không có quan hệ nào cần sửa.");

                                                return;
                                            }

                                            // Bước 4: Commit lần lượt từng batch.
                                            commitBatches(
                                                    batches,
                                                    0,
                                                    updated,
                                                    skipped
                                            );

                                        })
                                        .addOnFailureListener(e ->
                                                Log.e(TAG,
                                                        "Không đọc được story_categories",
                                                        e));

                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG,
                                            "Không đọc được categories",
                                            e));

                })
                .addOnFailureListener(e ->
                        Log.e(TAG,
                                "Không đọc được stories",
                                e));
    }

    // Commit tuần tự để dễ theo dõi lỗi từng batch.
    private void commitBatches(
            List<WriteBatch> batches,
            int index,
            int updatedCount,
            int skippedCount
    ) {

        if (index >= batches.size()) {

            Log.d(TAG,
                    "CẬP NHẬT FIREBASE THÀNH CÔNG!");

            Log.d(TAG,
                    "Số quan hệ đã cập nhật: " + updatedCount);

            Log.d(TAG,
                    "Số quan hệ bỏ qua: " + skippedCount);

            Log.d(TAG,
                    "Tổng số batch đã commit: " + batches.size());

            return;
        }

        Task<Void> commitTask = batches.get(index).commit();

        commitTask
                .addOnSuccessListener(unused -> {

                    Log.d(TAG,
                            "Commit batch "
                                    + (index + 1)
                                    + "/"
                                    + batches.size()
                                    + " thành công.");

                    commitBatches(
                            batches,
                            index + 1,
                            updatedCount,
                            skippedCount
                    );
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG,
                            "Commit batch "
                                    + (index + 1)
                                    + " thất bại. Dừng quá trình sửa.",
                            e);
                });
    }
}
