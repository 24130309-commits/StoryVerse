
package com.example.storyverse;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.storyverse.data.remote.firebase.RealtimeCatalogSyncManager;

public class MainActivity extends AppCompatActivity {

    private RealtimeCatalogSyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo bộ quản lý đồng bộ realtime
        syncManager = new RealtimeCatalogSyncManager(
                getApplicationContext()
        );

        // CẬP NHẬT DỮ LIỆU QUAN HỆ TRONG FIREBASE


        // Giữ nguyên xử lý WindowInsets
        View navHost = findViewById(R.id.nav_host_fragment);

        if (navHost != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    navHost, (view, windowInsets) -> {

                        Insets systemBars = windowInsets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        view.setPadding(
                                systemBars.left,
                                systemBars.top,
                                systemBars.right,
                                systemBars.bottom
                        );

                        return windowInsets;
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bắt đầu lắng nghe Firestore
        if (syncManager != null) {
            syncManager.start();
        }
    }

    @Override
    protected void onStop() {
        // Ngừng Listener khi Activity không còn hiển thị
        if (syncManager != null) {
            syncManager.stop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (syncManager != null) {
            syncManager.shutdown();
            syncManager = null;
        }

        super.onDestroy();
    }
}
