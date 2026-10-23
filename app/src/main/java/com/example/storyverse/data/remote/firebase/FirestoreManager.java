package com.example.storyverse.data.remote.firebase;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreManager {

    private final FirebaseFirestore firestore;

    public FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public CollectionReference getCollection(
            String collectionName
    ) {
        return firestore.collection(collectionName);
    }
}