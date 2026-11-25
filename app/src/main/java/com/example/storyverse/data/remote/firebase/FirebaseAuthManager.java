
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

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public FirebaseAuthManager(Context context) {
        Context appContext = context.getApplicationContext();

        if (FirebaseApp.getApps(appContext).isEmpty()) {
            FirebaseApp.initializeApp(appContext);
        }

        mAuth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public void register(String email, String password, AuthCallback callback) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Email hoặc mật khẩu không được để trống");
            }
            return;
        }

        mAuth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (callback != null) {
                            callback.onSuccess(user);
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Đăng ký thất bại"
                            );
                        }
                    }
                });
    }

    public void login(String email, String password, AuthCallback callback) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Email hoặc mật khẩu không được để trống");
            }
            return;
        }

        mAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (callback != null) {
                            callback.onSuccess(user);
                        }
                    } else {
                        if (callback != null) {
                            callback.onFailure(
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Đăng nhập thất bại"
                            );
                        }
                    }
                });
    }

    public void login(
            String email,
            String password,
            OnCompleteListener<AuthResult> listener
    ) {
        mAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(listener);
    }

    public void register(
            String fullName,
            String email,
            String password,
            OnCompleteListener<AuthResult> listener
    ) {
        mAuth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName.trim())
                                        .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask ->
                                        listener.onComplete(task));
                    } else {
                        listener.onComplete(task);
                    }
                });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void logout() {
        mAuth.signOut();
    }
}
