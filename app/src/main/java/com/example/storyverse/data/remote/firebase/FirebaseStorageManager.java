
package com.example.storyverse.data.remote.firebase;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageManager {

    private final FirebaseStorage storage;

    public FirebaseStorageManager() {
        storage = FirebaseStorage.getInstance();
    }

    // Callback dùng chung
    public interface StorageCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception error);
    }

    // Lấy FirebaseStorage
    public FirebaseStorage getStorage() {
        return storage;
    }

    // Lấy thư mục gốc
    public StorageReference getRootReference() {
        return storage.getReference();
    }

    // Lấy đường dẫn tham chiếu
    public StorageReference getReference(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn không hợp lệ");
        }

        return storage.getReference().child(path);
    }

    // 1. Tải ảnh lên Firebase Storage và trả về URL
    public void uploadImage(
            Uri imageUri,
            String storagePath,
            StorageCallback<String> callback
    ) {
        if (imageUri == null) {
            notifyFailure(callback, "Ảnh không hợp lệ");
            return;
        }

        if (storagePath == null || storagePath.trim().isEmpty()) {
            notifyFailure(callback, "Đường dẫn lưu ảnh không hợp lệ");
            return;
        }

        StorageReference imageRef = getReference(storagePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    if (callback != null) {
                                        callback.onSuccess(uri.toString());
                                    }
                                })
                                .addOnFailureListener(error ->
                                        notifyFailure(callback, error)
                                )
                )
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // 2. Lấy URL tải ảnh
    public void getDownloadUrl(
            String storagePath,
            StorageCallback<String> callback
    ) {
        if (storagePath == null || storagePath.trim().isEmpty()) {
            notifyFailure(callback, "Đường dẫn ảnh không hợp lệ");
            return;
        }

        getReference(storagePath)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    if (callback != null) {
                        callback.onSuccess(uri.toString());
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // 3. Xóa ảnh
    public void deleteImage(
            String storagePath,
            StorageCallback<Void> callback
    ) {
        if (storagePath == null || storagePath.trim().isEmpty()) {
            notifyFailure(callback, "Đường dẫn ảnh không hợp lệ");
            return;
        }

        getReference(storagePath)
                .delete()
                .addOnSuccessListener(unused -> {
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(error ->
                        notifyFailure(callback, error)
                );
    }

    // Xử lý lỗi
    private void notifyFailure(
            StorageCallback<?> callback,
            String message
    ) {
        if (callback != null) {
            callback.onFailure(
                    new IllegalArgumentException(message)
            );
        }
    }

    private void notifyFailure(
            StorageCallback<?> callback,
            Exception error
    ) {
        if (callback != null) {
            callback.onFailure(error);
        }
    }
}
