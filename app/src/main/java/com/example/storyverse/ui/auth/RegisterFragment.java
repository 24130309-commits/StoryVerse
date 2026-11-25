
package com.example.storyverse.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.remote.firebase.FirebaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterFragment extends Fragment {

    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialCheckBox cbTerms;
    private MaterialButton btnRegister;

    private FirebaseAuthManager authManager;

    public RegisterFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new FirebaseAuthManager();

        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etRegisterEmail);
        etPassword = view.findViewById(R.id.etRegisterPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        cbTerms = view.findViewById(R.id.cbTerms);
        btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> handleRegister());

        view.findViewById(R.id.tvLogin).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_registerFragment_to_loginFragment)
        );
    }

    private void handleRegister() {

        String fullName = etFullName.getText() != null
                ? etFullName.getText().toString().trim() : "";

        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";

        String password = etPassword.getText() != null
                ? etPassword.getText().toString() : "";

        String confirmPassword = etConfirmPassword.getText() != null
                ? etConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(
                    requireContext(),
                    "Vui lòng đồng ý với điều khoản sử dụng",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (authManager == null) {
            Toast.makeText(
                    requireContext(),
                    "Lỗi kết nối Firebase!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnRegister.setEnabled(false);

        Toast.makeText(
                requireContext(),
                "Đang tạo tài khoản...",
                Toast.LENGTH_SHORT
        ).show();

        authManager.register(fullName, email, password, task -> {

            if (!isAdded()) {
                return;
            }

            btnRegister.setEnabled(true);

            if (task.isSuccessful()) {

                authManager.signOut();

                Toast.makeText(
                        requireContext(),
                        "Đăng ký thành công! Vui lòng đăng nhập.",
                        Toast.LENGTH_LONG
                ).show();

                if (getView() != null) {
                    Navigation.findNavController(getView())
                            .navigate(
                                    R.id.action_registerFragment_to_loginFragment
                            );
                }

            } else {

                Exception e = task.getException();

                if (e instanceof FirebaseAuthUserCollisionException) {

                    etEmail.setError("Email này đã được đăng ký trước đó!");
                    etEmail.requestFocus();

                    Toast.makeText(
                            requireContext(),
                            "Email đã tồn tại!",
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    String errorMessage = e != null
                            ? e.getMessage()
                            : "Đăng ký thất bại. Vui lòng thử lại.";

                    Toast.makeText(
                            requireContext(),
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }
}
