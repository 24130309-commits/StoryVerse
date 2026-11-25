package com.example.storyverse.ui.ai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.storyverse.R;

import java.util.Locale;

public class AiFragment extends Fragment {

    private TextView tvChat;
    private EditText etQuestion;
    private ScrollView svChat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvChat = view.findViewById(R.id.tvAiChat);
        etQuestion = view.findViewById(R.id.etAiQuestion);
        svChat = view.findViewById(R.id.svAiChat);
        Button btnSend = view.findViewById(R.id.btnAiSend);

        btnSend.setOnClickListener(v -> sendQuestion());
    }

    private void sendQuestion() {
        String question = etQuestion.getText().toString().trim();
        if (TextUtils.isEmpty(question)) {
            etQuestion.setError("Hãy nhập câu hỏi");
            etQuestion.requestFocus();
            return;
        }

        String answer = generateAnswer(question);
        tvChat.append("\n\nBạn: " + question);
        tvChat.append("\nAI: " + answer);
        etQuestion.setText("");

        svChat.post(() -> svChat.fullScroll(View.FOCUS_DOWN));
    }

    private String generateAnswer(String question) {
        String text = question.toLowerCase(Locale.ROOT);

        if (text.contains("tình cảm") || text.contains("tình yêu") || text.contains("ngôn tình") || text.contains("lãng mạn")) {
            return "Bạn có thể thử các truyện tình cảm/ngôn tình. Gợi ý: chọn truyện có chủ đề tình yêu, gia đình hoặc cuộc sống để đọc thư giãn.";
        }

        if (text.contains("hài") || text.contains("cười") || text.contains("vui")) {
            return "Bạn thích truyện hài hước. Hãy mở mục Khám phá, chọn thể loại Hài hước và bắt đầu bằng truyện có số lượt thích cao.";
        }

        if (text.contains("trinh thám") || text.contains("bí ẩn") || text.contains("ma")) {
            return "Bạn phù hợp với truyện trinh thám hoặc bí ẩn. Hãy tìm truyện có mô tả về điều tra, vụ án, huyền bí hoặc siêu nhiên.";
        }

        if (text.contains("phiêu lưu") || text.contains("hành động") || text.contains("tu tiên") || text.contains("tiên hiệp")) {
            return "Bạn có thể đọc truyện phiêu lưu, hành động, kiếm hiệp hoặc tiên hiệp. Gợi ý: xem truyện đang hot và đọc thử chương đầu.";
        }

        if (text.contains("ngắn") || text.contains("ít thời gian")) {
            return "Hãy chọn truyện ngắn hoặc truyện có số chương ít. Bạn có thể xem mục Thư viện để lưu truyện đang đọc và tiếp tục sau.";
        }

        if (text.contains("xin chào") || text.contains("chào") || text.contains("hi")) {
            return "Xin chào! Tôi có thể gợi ý truyện theo thể loại: tình cảm, hài hước, trinh thám, phiêu lưu hoặc truyện ngắn.";
        }

        return "Mình chưa hiểu rõ thể loại bạn muốn. Thử nhập: ‘gợi ý truyện tình cảm’, ‘truyện hài hước’, ‘trinh thám’ hoặc ‘phiêu lưu’ nhé!";
    }
}