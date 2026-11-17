package com.example.storyverse.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.remote.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileFragment extends Fragment {

    private EditText etProfileName;
    private EditText etProfileEmail;
    private Button btnUpdateProfile;
    private Button btnLogout;
    private FirebaseAuthManager authManager;

    public ProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            authManager = new FirebaseAuthManager(requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        if (btnUpdateProfile != null) {
            btnUpdateProfile.setOnClickListener(v -> handleUpdateProfile());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout(v));
        }
    }

    private void loadUserData() {
        if (authManager == null) return;
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            if (etProfileEmail != null) {
                etProfileEmail.setText(user.getEmail());
                etProfileEmail.setEnabled(false); // Khóa ô Email không cho sửa bừa
            }
            if (etProfileName != null) {
                String name = !TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : "Người dùng StoryVerse";
                etProfileName.setText(name);
            }
        }
    }

    private void handleUpdateProfile() {
        if (etProfileName == null) return;
        String name = etProfileName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etProfileName.setError("Họ và tên không được để trống!");
            etProfileName.requestFocus();
            return;
        }

        if (authManager != null && authManager.getCurrentUser() != null) {
            btnUpdateProfile.setEnabled(false);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            authManager.getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (!isAdded()) return;
                        btnUpdateProfile.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi cập nhật: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void handleLogout(View view) {
        if (authManager != null) {
            authManager.logout();
        }
        Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_loginFragment);
    }
}