
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.storyverse.data.local.dao.ChapterDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.remote.firebase.ChapterFirestoreDataSource;
import com.example.storyverse.data.remote.firebase.FirestoreManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChapterRepository {

    private final ChapterDao chapterDao;
    private final ChapterFirestoreDataSource chapterFirestoreDataSource;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    // HTTP client dùng chung, tăng thời gian chờ tải ebook.
    private final OkHttpClient httpClient =
            new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(180, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

    public ChapterRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(
                        context.getApplicationContext()
                );

        chapterDao = database.chapterDao();
        chapterFirestoreDataSource = new ChapterFirestoreDataSource();
    }

    // =========================
    // INSERT
    // =========================

    public void insertChapter(
            ChapterEntity chapter,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (chapter == null) {
                    throw new IllegalArgumentException(
                            "Chapter không được null"
                    );
                }

                chapterDao.insertChapter(chapter);
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void insertChapters(
            List<ChapterEntity> chapters,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (chapters == null) {
                    throw new IllegalArgumentException(
                            "Danh sách chapter không được null"
                    );
                }

                if (!chapters.isEmpty()) {
                    chapterDao.insertChapters(chapters);
                }

                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    // =========================
    // GET FROM ROOM
    // =========================

    public void getChaptersByStoryId(
            String storyId,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {

        if (!isValidId(storyId)) {
            postError(
                    callback,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ChapterEntity> result =
                        chapterDao.getChaptersByStoryId(storyId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterById(
            String chapterId,
            RepositoryCallback<ChapterEntity> callback
    ) {

        if (!isValidId(chapterId)) {
            postError(
                    callback,
                    new IllegalArgumentException("Chapter ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                ChapterEntity result =
                        chapterDao.getChapterById(chapterId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterByNumber(
            String storyId,
            int chapterNumber,
            RepositoryCallback<ChapterEntity> callback
    ) {

        if (!isValidId(storyId) || chapterNumber < 1) {
            postError(
                    callback,
                    new IllegalArgumentException("Thông tin chương không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                ChapterEntity result =
                        chapterDao.getChapterByNumber(
                                storyId,
                                chapterNumber
                        );

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================
    // READER NAVIGATION / PAGING
    // =========================

    public void getChaptersByStoryIdPaged(
            String storyId,
            int limit,
            int offset,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {
        if (!isValidId(storyId)) {
            postError(callback, new IllegalArgumentException("Story ID không hợp lệ"));
            return;
        }
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safeOffset = Math.max(0, offset);
        executorService.execute(() -> {
            try {
                postSuccess(callback, chapterDao.getChaptersByStoryIdPaged(
                        storyId.trim(), safeLimit, safeOffset
                ));
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getNextChapter(
            String storyId,
            int currentChapterNumber,
            RepositoryCallback<ChapterEntity> callback
    ) {
        if (!isValidId(storyId) || currentChapterNumber < 1) {
            postError(callback, new IllegalArgumentException("Thông tin chương không hợp lệ"));
            return;
        }
        executorService.execute(() -> {
            try {
                postSuccess(callback, chapterDao.getNextChapter(
                        storyId.trim(), currentChapterNumber
                ));
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getPreviousChapter(
            String storyId,
            int currentChapterNumber,
            RepositoryCallback<ChapterEntity> callback
    ) {
        if (!isValidId(storyId) || currentChapterNumber < 1) {
            postError(callback, new IllegalArgumentException("Thông tin chương không hợp lệ"));
            return;
        }
        executorService.execute(() -> {
            try {
                postSuccess(callback, chapterDao.getPreviousChapter(
                        storyId.trim(), currentChapterNumber
                ));
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    // =========================
    // CACHE
    // =========================

    public void isChapterCached(
            String chapterId,
            RepositoryCallback<Boolean> callback
    ) {

        if (!isValidId(chapterId)) {
            postError(
                    callback,
                    new IllegalArgumentException("Chapter ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                boolean result =
                        chapterDao.isChapterCached(chapterId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void getChapterCountByStoryId(
            String storyId,
            RepositoryCallback<Integer> callback
    ) {

        if (!isValidId(storyId)) {
            postError(
                    callback,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                int result =
                        chapterDao.getChapterCountByStoryId(storyId);

                postSuccess(callback, result);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================
    // FIRESTORE -> ROOM
    // =========================

    public void fetchAndCacheChapters(
            String storyId,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        if (!isValidId(storyId)) {
            postError(
                    onError,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        chapterFirestoreDataSource.getChaptersByStoryId(
                storyId,
                new FirestoreManager.FirestoreCallback<List<ChapterEntity>>() {

                    @Override
                    public void onSuccess(List<ChapterEntity> chapters) {

                        executorService.execute(() -> {

                            try {
                                if (chapters == null) {
                                    throw new IllegalStateException(
                                            "Không nhận được danh sách chương từ Firestore"
                                    );
                                }

                                if (!chapters.isEmpty()) {
                                    chapterDao.insertChapters(chapters);
                                }

                                postComplete(onComplete);

                            } catch (Exception e) {
                                postError(onError, e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        postError(onError, e);
                    }
                }
        );
    }

    // =========================
    // CACHE FIRST, FIRESTORE FALLBACK
    // =========================

    public void getChaptersFromCacheOrFirestore(
            String storyId,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {

        if (!isValidId(storyId)) {
            postError(
                    callback,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                List<ChapterEntity> cachedChapters =
                        chapterDao.getChaptersByStoryId(storyId);

                if (cachedChapters != null && !cachedChapters.isEmpty()) {
                    postSuccess(callback, cachedChapters);
                    return;
                }

                chapterFirestoreDataSource.getChaptersByStoryId(
                        storyId,
                        new FirestoreManager.FirestoreCallback<List<ChapterEntity>>() {

                            @Override
                            public void onSuccess(List<ChapterEntity> result) {

                                executorService.execute(() -> {

                                    try {
                                        if (result == null) {
                                            throw new IllegalStateException(
                                                    "Danh sách chương không hợp lệ"
                                            );
                                        }

                                        if (!result.isEmpty()) {
                                            chapterDao.insertChapters(result);
                                        }

                                        postSuccess(callback, result);

                                    } catch (Exception e) {
                                        postError(callback, e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                postError(callback, e);
                            }
                        }
                );

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================================
    // LƯU TOÀN VĂN VÀO ROOM
    // =========================================

    public void saveFullTextChapter(
            String storyId,
            String chapterId,
            String title,
            String content,
            RepositoryCallback<ChapterEntity> callback
    ) {

        if (!isValidId(storyId)
                || !isValidId(chapterId)
                || !isValidId(title)
                || content == null
                || content.trim().isEmpty()) {

            postError(
                    callback,
                    new IllegalArgumentException(
                            "Thông tin toàn văn không hợp lệ"
                    )
            );
            return;
        }

        executorService.execute(() -> {

            try {
                String cleanContent = content.trim();

                int wordCount = cleanContent.split("\\s+").length;

                long currentTime = System.currentTimeMillis();

                ChapterEntity chapter = new ChapterEntity(
                        chapterId,
                        storyId,
                        1,
                        title.trim(),
                        cleanContent,
                        wordCount,
                        0,
                        currentTime,
                        currentTime
                );

                chapterDao.insertChapter(chapter);

                postSuccess(callback, chapter);

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =========================================
    // WIKISOURCE -> ROOM + FIRESTORE
    // =========================================

    public void fetchAndSaveWikisourceFullText(
            String storyId,
            String chapterId,
            String wikisourceTitle,
            RepositoryCallback<ChapterEntity> callback
    ) {

        if (!isValidId(storyId)
                || !isValidId(chapterId)
                || !isValidId(wikisourceTitle)) {

            postError(
                    callback,
                    new IllegalArgumentException(
                            "Thông tin tác phẩm không hợp lệ"
                    )
            );
            return;
        }

        WikisourceContentFetcher fetcher =
                new WikisourceContentFetcher();

        fetcher.fetch(
                wikisourceTitle,
                new WikisourceContentFetcher.FetchCallback() {

                    @Override
                    public void onSuccess(
                            String fetchedTitle,
                            String content
                    ) {

                        saveFullTextChapter(
                                storyId,
                                chapterId,
                                fetchedTitle,
                                content,
                                new RepositoryCallback<ChapterEntity>() {

                                    @Override
                                    public void onSuccess(
                                            ChapterEntity chapter
                                    ) {

                                        chapterFirestoreDataSource.saveChapter(
                                                chapter,
                                                new FirestoreManager.FirestoreCallback<Void>() {

                                                    @Override
                                                    public void onSuccess(Void result) {
                                                        postSuccess(callback, chapter);
                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        postError(callback, e);
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        postError(callback, e);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {
                        postError(callback, new Exception(message));
                    }
                }
        );
    }

    // =========================================
    // GUTENDEX -> ROOM + FIRESTORE
    // TẢI EBOOK CÔNG KHAI
    // =========================================

    public void fetchAndSaveGutendexBook(
            int bookId,
            String storyId,
            String chapterId,
            RepositoryCallback<ChapterEntity> callback
    ) {

        if (bookId <= 0
                || !isValidId(storyId)
                || !isValidId(chapterId)) {

            postError(
                    callback,
                    new IllegalArgumentException("Thông tin sách không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                Request infoRequest = new Request.Builder()
                        .url("https://gutendex.com/books/?ids=" + bookId)
                        .header("User-Agent", "STORYVERSE/1.0")
                        .build();

                String title;
                String downloadUrl = null;

                // BƯỚC 1: LẤY THÔNG TIN EBOOK
                try (Response response =
                             httpClient.newCall(infoRequest).execute()) {

                    if (!response.isSuccessful() || response.body() == null) {
                        throw new IOException(
                                "Không lấy được thông tin ebook. HTTP "
                                        + response.code()
                        );
                    }

                    JSONObject root =
                            new JSONObject(response.body().string());

                    JSONArray results = root.optJSONArray("results");

                    if (results == null || results.length() == 0) {
                        throw new IOException(
                                "Không tìm thấy ebook ID " + bookId
                        );
                    }

                    JSONObject book = results.getJSONObject(0);

                    // Chỉ tải toàn văn khi Gutendex xác nhận copyright=false.
                    // null/true được xem là chưa đủ căn cứ để sao chép nội dung.
                    if (book.isNull("copyright") || book.optBoolean("copyright", true)) {
                        throw new IOException(
                                "Không nhập toàn văn: nguồn không xác nhận sách thuộc phạm vi công cộng."
                        );
                    }

                    title = book.optString("title", "Untitled");

                    JSONObject formats = book.getJSONObject("formats");

                    Iterator<String> keys = formats.keys();

                    // Ưu tiên văn bản UTF-8.
                    while (keys.hasNext()) {
                        String key = keys.next();

                        if (key.startsWith("text/plain")
                                && key.toLowerCase().contains("utf-8")) {

                            downloadUrl = formats.optString(key);
                            break;
                        }
                    }

                    // Nếu không có UTF-8 thì lấy text/plain.
                    if (downloadUrl == null) {
                        keys = formats.keys();

                        while (keys.hasNext()) {
                            String key = keys.next();

                            if (key.startsWith("text/plain")) {
                                downloadUrl = formats.optString(key);
                                break;
                            }
                        }
                    }
                }

                if (downloadUrl == null || downloadUrl.isEmpty()) {
                    throw new IOException(
                            "Ebook không có định dạng văn bản phù hợp"
                    );
                }

                // BƯỚC 2: TẢI TOÀN VĂN
                Request contentRequest = new Request.Builder()
                        .url(downloadUrl)
                        .header("User-Agent", "STORYVERSE/1.0")
                        .build();

                String content;

                try (Response response =
                             httpClient.newCall(contentRequest).execute()) {

                    if (!response.isSuccessful() || response.body() == null) {
                        throw new IOException(
                                "Không tải được nội dung ebook. HTTP "
                                        + response.code()
                        );
                    }

                    content = response.body().string();
                }

                if (content.trim().isEmpty()) {
                    throw new IOException("Nội dung ebook trống");
                }

                LogGutendex("Đã tải nội dung: " + title);
                LogGutendex("Số ký tự tải được: " + content.length());

                // BƯỚC 3: LƯU ROOM
                saveFullTextChapter(
                        storyId,
                        chapterId,
                        title,
                        content,
                        new RepositoryCallback<ChapterEntity>() {

                            @Override
                            public void onSuccess(ChapterEntity chapter) {

                                // BƯỚC 4: ĐỒNG BỘ FIRESTORE
                                chapterFirestoreDataSource.saveChapter(
                                        chapter,
                                        new FirestoreManager.FirestoreCallback<Void>() {

                                            @Override
                                            public void onSuccess(Void result) {
                                                postSuccess(callback, chapter);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                postError(callback, e);
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onError(Exception e) {
                                postError(callback, e);
                            }
                        }
                );

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    private void LogGutendex(String message) {
        android.util.Log.d("STORYVERSE_IMPORT", message);
    }

    // =========================
    // DELETE
    // =========================

    public void deleteChapter(
            ChapterEntity chapter,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                if (chapter == null) {
                    throw new IllegalArgumentException(
                            "Chapter không được null"
                    );
                }

                chapterDao.deleteChapter(chapter);
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void deleteChaptersByStoryId(
            String storyId,
            Runnable onComplete,
            ErrorCallback onError
    ) {

        if (!isValidId(storyId)) {
            postError(
                    onError,
                    new IllegalArgumentException("Story ID không hợp lệ")
            );
            return;
        }

        executorService.execute(() -> {

            try {
                chapterDao.deleteChaptersByStoryId(storyId);
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    public void deleteAllChapters(
            Runnable onComplete,
            ErrorCallback onError
    ) {

        executorService.execute(() -> {

            try {
                chapterDao.deleteAllChapters();
                postComplete(onComplete);

            } catch (Exception e) {
                postError(onError, e);
            }
        });
    }

    // =========================
    // HELPERS
    // =========================

    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    private void postComplete(Runnable callback) {

        if (callback != null) {
            mainHandler.post(callback);
        }
    }

    private <T> void postSuccess(
            RepositoryCallback<T> callback,
            T result
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(result));
        }
    }

    private void postError(
            ErrorCallback callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    private <T> void postError(
            RepositoryCallback<T> callback,
            Exception error
    ) {

        if (callback != null) {
            mainHandler.post(() -> callback.onError(error));
        }
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public interface ErrorCallback {
        void onError(Exception e);
    }
}
