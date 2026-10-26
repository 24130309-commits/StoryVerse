package com.example.storyverse.utils;

import com.example.storyverse.utils.FirebaseAuthManager;

public class UserRepository {

    private final FirebaseAuthManager authManager;

    public UserRepository() {
        this.authManager = new FirebaseAuthManager();
    }

    public boolean isUserLoggedIn() {
        return authManager.getCurrentUser() != null;
    }
}