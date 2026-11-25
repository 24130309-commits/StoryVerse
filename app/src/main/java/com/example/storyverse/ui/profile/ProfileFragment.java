
package com.example.storyverse.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.remote.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvAvatar;
    private TextView tvName;
    private TextView tvEmail;

    private TextView tvStoryCount;
    private TextView tvChapterCount;
    private TextView tvFavoriteCount;

    private TextView tvEditProfile;
    private TextView tvFavorites;
    private TextView tvHistory;
    private TextView tvSettings;
    private TextView tvHelp;
    private TextView tvLogout;

    private FirebaseAuthManager authManager;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new FirebaseAuthManager();

        initViews(view);
        loadUserProfile();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        tvAvatar = view.findViewById(R.id.tv_profile_avatar);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);

        tvStoryCount = view.findViewById(R.id.tv_profile_story_count);
        tvChapterCount = view.findViewById(R.id.tv_profile_chapter_count);
        tvFavoriteCount = view.findViewById(R.id.tv_profile_favorite_count);

        tvEditProfile = view.findViewById(R.id.tv_edit_profile);
        tvFavorites = view.findViewById(R.id.tv_profile_favorites);
        tvHistory = view.findViewById(R.id.tv_profile_history);
        tvSettings = view.findViewById(R.id.tv_profile_settings);
        tvHelp = view.findViewById(R.id.tv_profile_help);
        tvLogout = view.findViewById(R.id.tv_profile_logout);
    }

    private void loadUserProfile() {
        FirebaseUser user = authManager.getCurrentUser();

        if (user == null) {
            tvName.setText("Khách");
            tvEmail.setText("Chưa đăng nhập");
            tvAvatar.setText("?");
            return;
        }

        String displayName = user.getDisplayName();
        String email = user.getEmail();

        if (TextUtils.isEmpty(displayName)) {
            displayName = !TextUtils.isEmpty(email)
                    ? email.substring(0, email.indexOf("@"))
                    : "Người dùng";
        }

        tvName.setText(displayName);
        tvEmail.setText(email != null ? email : "");

        String initial = displayName.trim().isEmpty()
                ? "S"
                : displayName.trim().substring(0, 1).toUpperCase();

        tvAvatar.setText(initial);

        // Chưa có dữ liệu thống kê thực tế, giữ giá trị mặc định.
        tvStoryCount.setText("0");
        tvChapterCount.setText("0");
        tvFavoriteCount.setText("0");
    }

    private void setupClickListeners(View view) {

        tvEditProfile.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Chức năng chỉnh sửa hồ sơ sẽ được triển khai sau.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        tvFavorites.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Danh sách truyện yêu thích sẽ được kết nối sau.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        tvHistory.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Lịch sử đọc sẽ được kết nối sau.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        tvSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        R.id.action_profileFragment_to_settingsFragment
                )
        );

        tvHelp.setOnClickListener(v ->
                Toast.makeText(
                        requireContext(),
                        "Chức năng hỗ trợ sẽ được triển khai sau.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        tvLogout.setOnClickListener(v -> showLogoutConfirmation(view));
    }

    private void showLogoutConfirmation(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi StoryVerse?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Đăng xuất", (dialog, which) ->
                        logout(view)
                )
                .show();
    }

    private void logout(View view) {
        authManager.signOut();

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .build();

        Navigation.findNavController(view).navigate(
                R.id.loginFragment,
                null,
                navOptions
        );
    }
}
