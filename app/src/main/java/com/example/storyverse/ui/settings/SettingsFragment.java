package com.example.storyverse.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.storyverse.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotification;
    private Button btnChangePassword;
    private Button btnAbout;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        switchNotification = view.findViewById(R.id.switchNotification);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnAbout = view.findViewById(R.id.btnAbout);

        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(getContext(), "Đã bật Chế độ tối", Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(getContext(), "Đã tắt Chế độ tối", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchNotification != null) {
            switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String status = isChecked ? "Đã bật thông báo" : "Đã tắt thông báo";
                Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v ->
                    Toast.makeText(getContext(), "Tính năng Đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v ->
                    Toast.makeText(getContext(), "StoryVerse v1.0 - App đọc truyện", Toast.LENGTH_LONG).show()
            );
        }
    }
}