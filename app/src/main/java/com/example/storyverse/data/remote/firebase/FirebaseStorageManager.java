package com.example.storyverse.data.remote.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageManager {

    private final FirebaseStorage storage;

    public FirebaseStorageManager() {
        storage = FirebaseStorage.getInstance();
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public StorageReference getRootReference() {
        return storage.getReference();
    }

    public StorageReference getReference(String path) {
        return storage.getReference(path);
    }
}