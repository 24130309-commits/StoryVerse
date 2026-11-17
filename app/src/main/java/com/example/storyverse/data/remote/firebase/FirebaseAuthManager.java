package com.example.storyverse.data.remote.firebase;

import android.content.Context;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthManager {

    private final FirebaseAuth mAuth;

    // Constructor mặc định (giữ lại để không lỗi các file khác)
    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Constructor có Context giúp chủ động khởi tạo Firebase nếu chưa tự chạy
    public FirebaseAuthManager(Context context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context);
        }
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener(listener);
    }

    public void register(String fullName, String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName.trim())
                                .build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> listener.onComplete(task));
                    } else {
                        listener.onComplete(task);
                    }
                });
    }

    public void logout() {
        mAuth.signOut();
    }
}