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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterFragment extends Fragment {

    private EditText etFullName;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvToLogin;
    private FirebaseAuthManager authManager;

    public RegisterFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            authManager = new FirebaseAuthManager(requireContext());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Chi tiết lỗi Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }



        etFullName = view.findViewById(R.id.etFullName);
        etRegisterEmail = view.findViewById(R.id.etRegisterEmail);
        etRegisterPassword = view.findViewById(R.id.etRegisterPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvToLogin = view.findViewById(R.id.tvToLogin);

        if (tvToLogin != null) {
            tvToLogin.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> handleRegister());
        }
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập Họ và tên!");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etRegisterEmail.setError("Vui lòng nhập Email!");
            etRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Email không đúng định dạng!");
            etRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etRegisterPassword.setError("Vui lòng nhập Mật khẩu!");
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Mật khẩu phải có ít nhất 6 ký tự!");
            etRegisterPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng nhập lại Mật khẩu!");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu nhập lại không khớp!");
            etConfirmPassword.requestFocus();
            return;
        }

        if (authManager == null) {
            Toast.makeText(getContext(), "Lỗi kết nối Firebase!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        Toast.makeText(getContext(), "Đang tạo tài khoản...", Toast.LENGTH_SHORT).show();

        authManager.register(fullName, email, password, task -> {
            if (!isAdded()) return;
            btnRegister.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                if (getView() != null) {
                    Navigation.findNavController(getView()).navigateUp();
                }
            } else {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthUserCollisionException) {
                    etRegisterEmail.setError("Email này đã được đăng ký trước đó!");
                    etRegisterEmail.requestFocus();
                    Toast.makeText(getContext(), "Đăng ký thất bại: Email đã tồn tại!", Toast.LENGTH_LONG).show();
                } else {
                    String msg = (e != null) ? e.getMessage() : "Đăng ký thất bại!";
                    Toast.makeText(getContext(), "Đăng ký thất bại: " + msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}