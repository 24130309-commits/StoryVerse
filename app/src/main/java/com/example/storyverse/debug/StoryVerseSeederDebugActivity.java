
package com.example.storyverse.debug;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.storyverse.data.remote.firebase.StoryVerseOldDataCleaner;

public class StoryVerseSeederDebugActivity extends Activity {

    private TextView statusText;
    private Button seedButton;
    private Button cleanButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("STORYVERSE - DATABASE MANAGER");
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);

        statusText = new TextView(this);
        statusText.setText("Sẵn sàng thao tác dữ liệu Firestore.");
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(ProgressBar.GONE);

        seedButton = new Button(this);
        seedButton.setText("NẠP 360 TRUYỆN");
        seedButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận nạp dữ liệu")
                        .setMessage(
                                "Tạo 360 truyện, 30 tác giả, " +
                                        "16 thể loại và 1.080 chương?"
                        )
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Đồng ý", (dialog, which) ->
                                runSeeder()
                        )
                        .show()
        );

        cleanButton = new Button(this);
        cleanButton.setText("DỌN DỮ LIỆU CŨ");
        cleanButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("CẢNH BÁO XÓA DỮ LIỆU")
                        .setMessage(
                                "Thao tác này sẽ xóa dữ liệu truyện cũ, " +
                                        "dữ liệu Open Library, tác giả cũ, " +
                                        "thể loại cũ và các quan hệ cũ.\n\n" +
                                        "Chỉ giữ bộ STORYVERSE_MOCK_360 " +
                                        "và dữ liệu người dùng được bảo vệ.\n\n" +
                                        "Bạn có chắc chắn muốn tiếp tục?"
                        )
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("XÓA DỮ LIỆU CŨ", (dialog, which) ->
                                runCleaner()
                        )
                        .show()
        );

        layout.addView(title);
        layout.addView(statusText);
        layout.addView(progressBar);
        layout.addView(seedButton);
        layout.addView(cleanButton);

        setContentView(layout);
    }

    private void runSeeder() {

        setBusy(true, "Đang nạp dữ liệu lên Firestore...");

        StoryVerseSeederDebug.run((success, message) ->
                runOnUiThread(() -> showResult(
                        success,
                        message,
                        "Nạp dữ liệu thành công",
                        "Nạp dữ liệu thất bại"
                ))
        );
    }

    private void runCleaner() {

        setBusy(true, "Đang dọn dữ liệu cũ trên Firestore...");

        StoryVerseOldDataCleaner.cleanOldData((success, message) ->
                runOnUiThread(() -> showResult(
                        success,
                        message,
                        "Dọn dữ liệu thành công",
                        "Dọn dữ liệu thất bại"
                ))
        );
    }

    private void setBusy(boolean busy, String message) {

        seedButton.setEnabled(!busy);
        cleanButton.setEnabled(!busy);

        progressBar.setVisibility(
                busy ? ProgressBar.VISIBLE : ProgressBar.GONE
        );

        statusText.setText(message);
    }

    private void showResult(
            boolean success,
            String message,
            String successTitle,
            String failureTitle
    ) {

        setBusy(false, message);

        new AlertDialog.Builder(this)
                .setTitle(success ? successTitle : failureTitle)
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .show();
    }
}
