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

    // Callback trả kết quả đăng ký và đăng nhập
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    // 1. Đăng ký tài khoản
    public void register(
            String email,
            String password,
            AuthCallback callback
    ) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Email hoặc mật khẩu không được để trống");
            }
            return;
        }

        mAuth.createUserWithEmailAndPassword(
                email.trim(),
                password
        ).addOnCompleteListener(task -> {
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

    // 2. Đăng nhập
    public void login(
            String email,
            String password,
            AuthCallback callback
    ) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Email hoặc mật khẩu không được để trống");
            }
            return;
        }

        mAuth.signInWithEmailAndPassword(
                email.trim(),
                password
        ).addOnCompleteListener(task -> {
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

    // 3. Lấy người dùng hiện tại
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // 4. Lấy UID người dùng
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();

        return user != null ? user.getUid() : null;
    }

    // 5. Kiểm tra trạng thái đăng nhập
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    // 6. Đăng nhập bằng listener
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener(listener);
    }

    // 7. Đăng ký bằng listener
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
