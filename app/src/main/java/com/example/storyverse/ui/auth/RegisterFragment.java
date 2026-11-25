
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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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

        btnRegister.setEnabled(false);

        authManager.getFirebaseAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!isAdded()) {
                        return;
                    }

                    if (task.isSuccessful()) {

                        FirebaseUser user = task.getResult().getUser();

                        if (user != null) {

                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(fullName)
                                            .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {

                                        if (!isAdded()) {
                                            return;
                                        }

                                        authManager.signOut();
                                        btnRegister.setEnabled(true);

                                        Toast.makeText(
                                                requireContext(),
                                                "Đăng ký thành công! Vui lòng đăng nhập.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        Navigation.findNavController(requireView())
                                                .navigate(R.id.action_registerFragment_to_loginFragment);
                                    });

                        } else {
                            authManager.signOut();
                            btnRegister.setEnabled(true);

                            Toast.makeText(
                                    requireContext(),
                                    "Không thể lấy thông tin tài khoản.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    } else {

                        btnRegister.setEnabled(true);

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng ký thất bại. Vui lòng thử lại.";

                        Toast.makeText(
                                requireContext(),
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
