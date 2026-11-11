
package com.example.storyverse.data.repository;

import com.example.storyverse.data.remote.parser.WikisourceHtmlParser;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WikisourceContentFetcher {

    private static final String BASE_URL =
            "https://vi.wikisource.org/w/api.php";

    private final OkHttpClient client = new OkHttpClient();

    public interface FetchCallback {
        void onSuccess(String title, String content);
        void onError(String message);
    }

    public void fetch(String title, FetchCallback callback) {

        if (callback == null) {
            return;
        }

        if (title == null || title.trim().isEmpty()) {
            callback.onError("Tên tác phẩm không được để trống");
            return;
        }

        new Thread(() -> {

            try {
                String encodedTitle = URLEncoder.encode(
                        title.trim(),
                        StandardCharsets.UTF_8.name()
                );

                String url = BASE_URL
                        + "?action=parse"
                        + "&page=" + encodedTitle
                        + "&prop=text"
                        + "&format=json"
                        + "&formatversion=2";

                Request request = new Request.Builder()
                        .url(url)
                        .header(
                                "User-Agent",
                                "STORYVERSE/1.0 (https://github.com/24130309-commits/StoryVerse)"
                        )
                        .build();

                try (Response response =
                             client.newCall(request).execute()) {

                    if (!response.isSuccessful()
                            || response.body() == null) {
                        throw new IOException(
                                "HTTP: " + response.code()
                        );
                    }

                    String json = response.body().string();

                    JSONObject root = new JSONObject(json);

                    if (root.has("error")) {
                        throw new Exception(
                                root.getJSONObject("error").optString(
                                        "info",
                                        "Không tìm thấy tác phẩm"
                                )
                        );
                    }

                    JSONObject parse = root.getJSONObject("parse");

                    // parse.text là chuỗi HTML, không phải JSON object.
                    String html = parse.optString("text", "");

                    if (html.trim().isEmpty()) {
                        throw new Exception(
                                "Wikisource không trả về HTML"
                        );
                    }

                    // Chỉ trích xuất phần nội dung tác phẩm.
                    String content =
                            WikisourceHtmlParser.extractPoemText(html);

                    if (content.isEmpty()) {
                        throw new Exception(
                                "Không tìm thấy phần nội dung div.poem"
                        );
                    }

                    String resultTitle =
                            parse.optString("title", title);

                    android.os.Handler mainHandler =
                            new android.os.Handler(
                                    android.os.Looper.getMainLooper()
                            );

                    mainHandler.post(() ->
                            callback.onSuccess(resultTitle, content)
                    );
                }

            } catch (Exception e) {

                String message = e.getMessage() != null
                        ? e.getMessage()
                        : "Không thể lấy nội dung Wikisource";

                new android.os.Handler(
                        android.os.Looper.getMainLooper()
                ).post(() -> callback.onError(message));
            }

        }).start();
    }
}
