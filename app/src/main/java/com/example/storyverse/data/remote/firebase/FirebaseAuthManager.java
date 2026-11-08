
package com.example.storyverse.data.remote.firebase;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
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

        firebaseAuth.createUserWithEmailAndPassword(
                email.trim(),
                password
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

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

        firebaseAuth.signInWithEmailAndPassword(
                email.trim(),
                password
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

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
        return firebaseAuth.getCurrentUser();
    }

    // 4. Lấy UID người dùng
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        return user != null ? user.getUid() : null;
    }

    // 5. Kiểm tra trạng thái đăng nhập
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    // 6. Đăng xuất
    public void signOut() {
        firebaseAuth.signOut();
    }

    // 7. Truy cập FirebaseAuth khi cần thiết
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }
}
