
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
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.remote.firebase.FirebaseAuthManager;

public class LoginFragment extends Fragment {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuthManager authManager;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new FirebaseAuthManager();

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> handleLogin(v));

        tvRegister.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    private void handleLogin(View view) {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

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

        btnLogin.setEnabled(false);

        authManager.getFirebaseAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!isAdded()) {
                        return;
                    }

                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                requireContext(),
                                "Đăng nhập thành công!",
                                Toast.LENGTH_SHORT
                        ).show();

                        NavOptions navOptions = new NavOptions.Builder()
                                .setPopUpTo(R.id.loginFragment, true)
                                .build();

                        Navigation.findNavController(view)
                                .navigate(
                                        R.id.action_loginFragment_to_homeFragment2,
                                        null,
                                        navOptions
                                );

                    } else {

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại. Vui lòng thử lại.";

                        Toast.makeText(
                                requireContext(),
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
