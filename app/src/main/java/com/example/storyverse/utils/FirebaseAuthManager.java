package com.example.storyverse.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    private final FirebaseAuth mAuth;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void logout() {
        mAuth.signOut();
    }
}
