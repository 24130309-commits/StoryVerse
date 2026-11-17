package com.example.storyverse.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.remote.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginFragment extends Fragment {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuthManager authManager;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            authManager = new FirebaseAuthManager();
        } catch (Exception e) {
            e.printStackTrace();
        }

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleLogin());
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v ->
                    Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment)
            );
        }
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập Email!");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không đúng định dạng!");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập Mật khẩu!");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự!");
            etPassword.requestFocus();
            return;
        }

        if (authManager == null) {
            Toast.makeText(getContext(), "Lỗi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        Toast.makeText(getContext(), "Đang xác thực...", Toast.LENGTH_SHORT).show();

        authManager.login(email, password, task -> {
            if (!isAdded()) return;
            btnLogin.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment2);
                }
            } else {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthInvalidUserException) {
                    etEmail.setError("Email này chưa được đăng ký!");
                    etEmail.requestFocus();
                    Toast.makeText(getContext(), "Tài khoản không tồn tại!", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    etPassword.setError("Sai email hoặc mật khẩu!");
                    etPassword.requestFocus();
                    Toast.makeText(getContext(), "Sai email hoặc mật khẩu!", Toast.LENGTH_LONG).show();
                } else {
                    String msg = (e != null) ? e.getMessage() : "Đăng nhập thất bại!";
                    Toast.makeText(getContext(), "Đăng nhập thất bại: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}