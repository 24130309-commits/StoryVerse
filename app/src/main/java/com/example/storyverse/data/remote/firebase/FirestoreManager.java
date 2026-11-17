
package com.example.storyverse.data.remote.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class FirestoreManager {

    private final FirebaseFirestore firestore;

    public FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Callback dùng chung cho các thao tác Firestore
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception error);
    }

    // Lấy FirebaseFirestore
    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    // Lấy Collection
    public CollectionReference getCollection(String collectionName) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên collection không hợp lệ");
        }

        return firestore.collection(collectionName);
    }

    // 1. Thêm tài liệu với ID tự động
    public void addDocument(
            String collectionName,
            Map<String, Object> data,
            FirestoreCallback<String> callback
    ) {
        if (!isValidData(collectionName, data, callback)) return;

        getCollection(collectionName)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) {
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // 2. Thêm hoặc ghi đè tài liệu với ID chỉ định
    public void setDocument(
            String collectionName,
            String documentId,
            Map<String, Object> data,
            FirestoreCallback<Void> callback
    ) {
        if (!isValidDocumentData(collectionName, documentId, data, callback)) return;

        getCollection(collectionName)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // 3. Đọc một tài liệu theo ID
    public void getDocument(
            String collectionName,
            String documentId,
            FirestoreCallback<DocumentSnapshot> callback
    ) {
        if (!isValidDocument(collectionName, documentId, callback)) return;

        getCollection(collectionName)
                .document(documentId)
                .get()
                .addOnSuccessListener(document -> {
                    if (callback != null) {
                        callback.onSuccess(document);
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // 4. Đọc toàn bộ tài liệu trong Collection
    public void getCollectionDocuments(
            String collectionName,
            FirestoreCallback<QuerySnapshot> callback
    ) {
        if (!isValidCollection(collectionName, callback)) return;

        getCollection(collectionName)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (callback != null) {
                        callback.onSuccess(snapshot);
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // 5. Cập nhật các trường của tài liệu
    public void updateDocument(
            String collectionName,
            String documentId,
            Map<String, Object> updates,
            FirestoreCallback<Void> callback
    ) {
        if (!isValidDocumentData(collectionName, documentId, updates, callback)) return;

        getCollection(collectionName)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // 6. Xóa tài liệu
    public void deleteDocument(
            String collectionName,
            String documentId,
            FirestoreCallback<Void> callback
    ) {
        if (!isValidDocument(collectionName, documentId, callback)) return;

        getCollection(collectionName)
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error -> {
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                });
    }

    // Kiểm tra dữ liệu
    private boolean isValidData(
            String collectionName,
            Map<String, Object> data,
            FirestoreCallback<?> callback
    ) {
        if (!isValidCollection(collectionName, callback)) return false;

        if (data == null || data.isEmpty()) {
            notifyFailure(callback, "Dữ liệu không được để trống");
            return false;
        }

        return true;
    }

    private boolean isValidDocumentData(
            String collectionName,
            String documentId,
            Map<String, Object> data,
            FirestoreCallback<?> callback
    ) {
        if (!isValidDocument(collectionName, documentId, callback)) return false;

        if (data == null || data.isEmpty()) {
            notifyFailure(callback, "Dữ liệu không được để trống");
            return false;
        }

        return true;
    }

    private boolean isValidDocument(
            String collectionName,
            String documentId,
            FirestoreCallback<?> callback
    ) {
        if (!isValidCollection(collectionName, callback)) return false;

        if (documentId == null || documentId.trim().isEmpty()) {
            notifyFailure(callback, "Document ID không hợp lệ");
            return false;
        }

        return true;
    }

    private boolean isValidCollection(
            String collectionName,
            FirestoreCallback<?> callback
    ) {
        if (collectionName == null || collectionName.trim().isEmpty()) {
            notifyFailure(callback, "Tên collection không hợp lệ");
            return false;
        }

        return true;
    }

    private void notifyFailure(
            FirestoreCallback<?> callback,
            String message
    ) {
        if (callback != null) {
            callback.onFailure(new IllegalArgumentException(message));
        }
    }
}
