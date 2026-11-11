package com.example.storyverse.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.storyverse.data.mapper.GoogleBooksMapper;
import com.example.storyverse.data.mapper.OpenLibraryMapper;
import com.example.storyverse.data.mapper.StoryDeduplicator;
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.api.googlebooks.GoogleBooksResponse;
import com.example.storyverse.data.remote.api.openlibrary.OpenLibraryResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiSourceStoryRepository {

    private static final int DEFAULT_PAGE_COUNT = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;

    private static final int MAX_PAGE_COUNT = 3;
    private static final int MAX_PAGE_SIZE = 40;

    private static final int COLLECTION_PAGE_COUNT = 1;
    private static final int COLLECTION_PAGE_SIZE = 12;
    private static final int MAX_STORIES_PER_GENRE = 3;
    private static final int MAX_COLLECTION_STORIES = 63;

    private final GoogleBooksRepository googleBooksRepository;
    private final OpenLibraryRepository openLibraryRepository;
    private final NarouRepository narouRepository;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface MultiSourceCallback {

        void onSuccess(
                List<Story> stories,
                List<String> warnings
        );

        void onError(String message);
    }

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public MultiSourceStoryRepository() {

        googleBooksRepository = new GoogleBooksRepository();
        openLibraryRepository = new OpenLibraryRepository();
        narouRepository = new NarouRepository();
    }

    // =========================================
    // 1. TÌM KIẾM TRUYỆN
    // =========================================

    public void searchStories(
            String keyword,
            MultiSourceCallback callback
    ) {

        searchStories(
                keyword,
                DEFAULT_PAGE_COUNT,
                DEFAULT_PAGE_SIZE,
                callback
        );
    }

    public void searchStories(
            String keyword,
            int pageCount,
            int pageSize,
            MultiSourceCallback callback
    ) {

        searchStoriesInternal(
                keyword,
                pageCount,
                pageSize,
                true,
                callback
        );
    }

    // =========================================
    // 2. TÌM KIẾM RIÊNG NAROU
    // =========================================

    public void searchNarouStories(
            String keyword,
            int page,
            int pageSize,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        narouRepository.searchWebNovels(
                keyword,
                page,
                pageSize,
                new NarouRepository.CallbackResult() {

                    @Override
                    public void onSuccess(List<Story> stories) {

                        List<Story> unique =
                                StoryDeduplicator.removeDuplicates(stories);

                        mainHandler.post(() ->
                                callback.onSuccess(
                                        unique,
                                        new ArrayList<>()
                                )
                        );
                    }

                    @Override
                    public void onError(String message) {

                        mainHandler.post(() ->
                                callback.onError(message)
                        );
                    }
                }
        );
    }

    // =========================================
    // 3. DANH SÁCH 21 THỂ LOẠI STORYVERSE
    // =========================================

    public static List<String> getDefaultGenreKeywords() {

        return Collections.unmodifiableList(
                Arrays.asList(
                        "tiên hiệp",
                        "kiếm hiệp",
                        "huyền huyễn",
                        "dị giới",
                        "phiêu lưu",
                        "hành động",
                        "đô thị",
                        "học đường",
                        "thanh xuân",
                        "ngôn tình",
                        "đam mỹ",
                        "xuyên không",
                        "trọng sinh",
                        "trinh thám",
                        "kinh dị",
                        "tâm lý",
                        "khoa huyễn",
                        "quân sự",
                        "lịch sử",
                        "cung đấu",
                        "hài hước"
                )
        );
    }

    // =========================================
    // 4. THU THẬP TRUYỆN THEO THỂ LOẠI
    // =========================================

    public void collectGenreStories(
            MultiSourceCallback callback
    ) {

        collectGenreStories(
                getDefaultGenreKeywords(),
                COLLECTION_PAGE_COUNT,
                COLLECTION_PAGE_SIZE,
                callback
        );
    }

    public void collectGenreStories(
            List<String> genreKeywords,
            int pageCount,
            int pageSize,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        searchStoriesByKeywords(
                genreKeywords,
                pageCount,
                pageSize,
                new MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {

                        collectNarouAndMerge(
                                stories,
                                warnings,
                                callback
                        );
                    }

                    @Override
                    public void onError(String message) {

                        List<String> warnings = new ArrayList<>();
                        warnings.add(message);

                        collectNarouAndMerge(
                                new ArrayList<>(),
                                warnings,
                                callback
                        );
                    }
                }
        );
    }

    // =========================================
    // 5. KẾT HỢP KẾT QUẢ NAROU VỚI CÁC NGUỒN
    // =========================================

    private void collectNarouAndMerge(
            List<Story> collected,
            List<String> warnings,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        narouRepository.searchWebNovels(
                null,
                1,
                30,
                new NarouRepository.CallbackResult() {

                    @Override
                    public void onSuccess(List<Story> narouStories) {

                        List<Story> combined = new ArrayList<>();

                        if (collected != null) {
                            combined.addAll(collected);
                        }

                        if (narouStories != null) {
                            combined.addAll(narouStories);
                        }

                        List<Story> unique =
                                StoryDeduplicator.removeDuplicates(combined);

                        List<String> resultWarnings =
                                warnings == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(warnings);

                        mainHandler.post(() ->
                                callback.onSuccess(
                                        unique,
                                        resultWarnings
                                )
                        );
                    }

                    @Override
                    public void onError(String message) {

                        List<Story> safeStories =
                                collected == null
                                        ? new ArrayList<>()
                                        : collected;

                        List<String> resultWarnings =
                                warnings == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(warnings);

                        resultWarnings.add(
                                "Narou: " + safeMessage(message)
                        );

                        if (!safeStories.isEmpty()) {

                            List<Story> unique =
                                    StoryDeduplicator.removeDuplicates(
                                            safeStories
                                    );

                            mainHandler.post(() ->
                                    callback.onSuccess(
                                            unique,
                                            resultWarnings
                                    )
                            );

                        } else {

                            mainHandler.post(() ->
                                    callback.onError(
                                            buildErrorMessage(
                                                    resultWarnings,
                                                    "Không thu thập được truyện"
                                            )
                                    )
                            );
                        }
                    }
                }
        );
    }

    // =========================================
    // 6. TÌM KIẾM NHIỀU TỪ KHÓA
    // =========================================

    public void searchStoriesByKeywords(
            List<String> keywords,
            int pageCount,
            int pageSize,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (keywords == null || keywords.isEmpty()) {

            mainHandler.post(() ->
                    callback.onError(
                            "Danh sách từ khóa không được để trống"
                    )
            );

            return;
        }

        Set<String> uniqueKeywords = new LinkedHashSet<>();

        for (String keyword : keywords) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                uniqueKeywords.add(keyword.trim());
            }
        }

        if (uniqueKeywords.isEmpty()) {

            mainHandler.post(() ->
                    callback.onError(
                            "Không có từ khóa hợp lệ"
                    )
            );

            return;
        }

        int safePageCount = Math.max(
                1,
                Math.min(pageCount, MAX_PAGE_COUNT)
        );

        int safePageSize = Math.max(
                1,
                Math.min(pageSize, MAX_PAGE_SIZE)
        );

        List<String> keywordList =
                new ArrayList<>(uniqueKeywords);

        List<Story> collectedStories =
                Collections.synchronizedList(new ArrayList<>());

        List<String> warnings =
                Collections.synchronizedList(new ArrayList<>());

        searchKeywordAtIndex(
                keywordList,
                0,
                safePageCount,
                safePageSize,
                collectedStories,
                warnings,
                callback
        );
    }

    // =========================================
    // 7. DUYỆT TỪNG THỂ LOẠI
    // =========================================

    private void searchKeywordAtIndex(
            List<String> keywords,
            int index,
            int pageCount,
            int pageSize,
            List<Story> collectedStories,
            List<String> warnings,
            MultiSourceCallback callback
    ) {

        if (index >= keywords.size()
                || collectedStories.size() >= MAX_COLLECTION_STORIES) {

            finishCollection(
                    collectedStories,
                    warnings,
                    callback
            );

            return;
        }

        String keyword = keywords.get(index);

        searchStoriesInternal(
                keyword,
                pageCount,
                pageSize,
                false,
                new MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> searchWarnings
                    ) {

                        if (stories != null) {

                            int added = 0;

                            for (Story story : stories) {

                                if (story == null) {
                                    continue;
                                }

                                if (added >= MAX_STORIES_PER_GENRE
                                        || collectedStories.size()
                                        >= MAX_COLLECTION_STORIES) {

                                    break;
                                }

                                attachDefaultGenre(story, keyword);

                                collectedStories.add(story);
                                added++;
                            }
                        }

                        if (searchWarnings != null) {
                            warnings.addAll(searchWarnings);
                        }

                        // Nghỉ ngắn giữa các từ khóa để tránh
                        // gửi quá nhiều request liên tục.
                        mainHandler.postDelayed(
                                () -> searchKeywordAtIndex(
                                        keywords,
                                        index + 1,
                                        pageCount,
                                        pageSize,
                                        collectedStories,
                                        warnings,
                                        callback
                                ),
                                1100L
                        );
                    }

                    @Override
                    public void onError(String message) {

                        warnings.add(
                                "Từ khóa '" + keyword + "': "
                                        + safeMessage(message)
                        );

                        mainHandler.postDelayed(
                                () -> searchKeywordAtIndex(
                                        keywords,
                                        index + 1,
                                        pageCount,
                                        pageSize,
                                        collectedStories,
                                        warnings,
                                        callback
                                ),
                                1100L
                        );
                    }
                }
        );
    }

    // =========================================
    // 8. GẮN THỂ LOẠI CHO TRUYỆN
    // =========================================

    private void attachDefaultGenre(
            Story story,
            String keyword
    ) {

        if (story == null || keyword == null) {
            return;
        }

        String canonicalGenre = null;

        for (String genre : getDefaultGenreKeywords()) {

            if (genre.equalsIgnoreCase(keyword.trim())) {
                canonicalGenre = genre;
                break;
            }
        }

        if (canonicalGenre == null) {
            return;
        }

        List<String> categories = story.getCategories();

        if (categories == null) {
            categories = new ArrayList<>();
        } else {
            categories = new ArrayList<>(categories);
        }

        for (String existing : categories) {

            if (existing != null
                    && existing.trim().equalsIgnoreCase(canonicalGenre)) {

                story.setCategories(categories);
                return;
            }
        }

        categories.add(canonicalGenre);
        story.setCategories(categories);
    }

    // =========================================
    // 9. HOÀN TẤT THU THẬP
    // =========================================

    private void finishCollection(
            List<Story> collectedStories,
            List<String> warnings,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        List<Story> storySnapshot;
        List<String> warningSnapshot;

        synchronized (collectedStories) {
            storySnapshot = new ArrayList<>(collectedStories);
        }

        synchronized (warnings) {
            warningSnapshot = new ArrayList<>(warnings);
        }

        List<Story> uniqueStories =
                StoryDeduplicator.removeDuplicates(storySnapshot);

        if (uniqueStories.isEmpty()) {

            mainHandler.post(() ->
                    callback.onError(
                            buildErrorMessage(
                                    warningSnapshot,
                                    "Không thu thập được truyện từ các nguồn API"
                            )
                    )
            );

            return;
        }

        mainHandler.post(() ->
                callback.onSuccess(
                        uniqueStories,
                        warningSnapshot
                )
        );
    }

    // =========================================
    // 10. TÌM KIẾM MỘT TỪ KHÓA TỪ HAI NGUỒN
    // =========================================

    private void searchStoriesInternal(
            String keyword,
            int pageCount,
            int pageSize,
            boolean filterVietnamese,
            MultiSourceCallback callback
    ) {

        if (callback == null) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {

            mainHandler.post(() ->
                    callback.onError(
                            "Từ khóa tìm kiếm không được để trống"
                    )
            );

            return;
        }

        String searchKeyword = keyword.trim();

        int safePageCount = Math.max(
                1,
                Math.min(pageCount, MAX_PAGE_COUNT)
        );

        int safePageSize = Math.max(
                1,
                Math.min(pageSize, MAX_PAGE_SIZE)
        );

        List<Story> allStories =
                Collections.synchronizedList(new ArrayList<>());

        List<String> warnings =
                Collections.synchronizedList(new ArrayList<>());

        AtomicInteger completedSources = new AtomicInteger(0);

        Runnable finishSource = () -> {

            if (completedSources.incrementAndGet() == 2) {

                List<Story> storySnapshot;
                List<String> warningSnapshot;

                synchronized (allStories) {
                    storySnapshot = new ArrayList<>(allStories);
                }

                synchronized (warnings) {
                    warningSnapshot = new ArrayList<>(warnings);
                }

                mainHandler.post(() ->
                        processResults(
                                storySnapshot,
                                warningSnapshot,
                                filterVietnamese,
                                callback
                        )
                );
            }
        };

        Runnable finishGoogle =
                createOnceRunnable(finishSource);

        Runnable finishOpenLibrary =
                createOnceRunnable(finishSource);

        loadGooglePages(
                searchKeyword,
                1,
                safePageCount,
                safePageSize,
                allStories,
                warnings,
                finishGoogle
        );

        loadOpenLibraryPages(
                searchKeyword,
                1,
                safePageCount,
                safePageSize,
                allStories,
                warnings,
                finishOpenLibrary
        );
    }

    // =========================================
    // 11. ĐẢM BẢO MỖI NGUỒN CHỈ KẾT THÚC MỘT LẦN
    // =========================================

    private Runnable createOnceRunnable(Runnable action) {

        AtomicBoolean completed = new AtomicBoolean(false);

        return () -> {

            if (completed.compareAndSet(false, true)) {
                action.run();
            }
        };
    }

    // =========================================
    // 12. GOOGLE BOOKS
    // =========================================

    private void loadGooglePages(
            String keyword,
            int currentPage,
            int maxPages,
            int pageSize,
            List<Story> allStories,
            List<String> warnings,
            Runnable onFinished
    ) {

        if (currentPage > maxPages) {
            onFinished.run();
            return;
        }

        try {

            googleBooksRepository.searchBooks(
                    keyword,
                    currentPage,
                    pageSize,
                    new GoogleBooksRepository.BooksCallback() {

                        @Override
                        public void onSuccess(
                                GoogleBooksResponse response
                        ) {

                            try {

                                if (response != null
                                        && response.items != null
                                        && !response.items.isEmpty()) {

                                    List<Story> mapped =
                                            GoogleBooksMapper.toStoryList(
                                                    response.items
                                            );

                                    if (mapped != null) {
                                        allStories.addAll(mapped);
                                    }
                                }

                                loadGooglePages(
                                        keyword,
                                        currentPage + 1,
                                        maxPages,
                                        pageSize,
                                        allStories,
                                        warnings,
                                        onFinished
                                );

                            } catch (Exception e) {

                                warnings.add(
                                        "Google Books: "
                                                + getErrorMessage(e)
                                );

                                onFinished.run();
                            }
                        }

                        @Override
                        public void onError(String message) {

                            warnings.add(
                                    "Google Books: "
                                            + safeMessage(message)
                            );

                            onFinished.run();
                        }
                    }
            );

        } catch (Exception e) {

            warnings.add(
                    "Google Books: " + getErrorMessage(e)
            );

            onFinished.run();
        }
    }

    // =========================================
    // 13. OPEN LIBRARY
    // =========================================

    private void loadOpenLibraryPages(
            String keyword,
            int currentPage,
            int maxPages,
            int pageSize,
            List<Story> allStories,
            List<String> warnings,
            Runnable onFinished
    ) {

        if (currentPage > maxPages) {
            onFinished.run();
            return;
        }

        try {

            openLibraryRepository.searchBooks(
                    keyword,
                    currentPage,
                    pageSize,
                    new OpenLibraryRepository.BooksCallback() {

                        @Override
                        public void onSuccess(
                                OpenLibraryResponse response
                        ) {

                            try {

                                if (response != null
                                        && response.docs != null
                                        && !response.docs.isEmpty()) {

                                    List<Story> mapped =
                                            OpenLibraryMapper.toStoryList(
                                                    response.docs
                                            );

                                    if (mapped != null) {
                                        allStories.addAll(mapped);
                                    }
                                }

                                loadOpenLibraryPages(
                                        keyword,
                                        currentPage + 1,
                                        maxPages,
                                        pageSize,
                                        allStories,
                                        warnings,
                                        onFinished
                                );

                            } catch (Exception e) {

                                warnings.add(
                                        "Open Library: "
                                                + getErrorMessage(e)
                                );

                                onFinished.run();
                            }
                        }

                        @Override
                        public void onError(String message) {

                            warnings.add(
                                    "Open Library: "
                                            + safeMessage(message)
                            );

                            onFinished.run();
                        }
                    }
            );

        } catch (Exception e) {

            warnings.add(
                    "Open Library: " + getErrorMessage(e)
            );

            onFinished.run();
        }
    }

    // =========================================
    // 14. XỬ LÝ KẾT QUẢ VÀ LOẠI TRÙNG
    // =========================================

    private void processResults(
            List<Story> allStories,
            List<String> warnings,
            boolean filterVietnamese,
            MultiSourceCallback callback
    ) {

        if (allStories == null || allStories.isEmpty()) {

            callback.onError(
                    buildErrorMessage(
                            warnings,
                            "Không tìm thấy truyện từ các nguồn API"
                    )
            );

            return;
        }

        List<Story> filteredStories = new ArrayList<>();

        for (Story story : allStories) {

            if (story == null) {
                continue;
            }

            if (!filterVietnamese || isVietnameseOrUnknown(story)) {
                filteredStories.add(story);
            }
        }

        if (filteredStories.isEmpty()) {

            callback.onError(
                    buildErrorMessage(
                            warnings,
                            "Không tìm thấy truyện phù hợp"
                    )
            );

            return;
        }

        List<Story> uniqueStories =
                StoryDeduplicator.removeDuplicates(filteredStories);

        if (uniqueStories.isEmpty()) {

            callback.onError(
                    "Không tìm thấy truyện phù hợp sau khi xử lý dữ liệu"
            );

            return;
        }

        callback.onSuccess(
                uniqueStories,
                new ArrayList<>(warnings)
        );
    }

    // =========================================
    // 15. LỌC NGÔN NGỮ
    // =========================================

    private boolean isVietnameseOrUnknown(Story story) {

        if (story == null) {
            return false;
        }

        String language = story.getLanguage();

        if (language == null || language.trim().isEmpty()) {
            return true;
        }

        language = language.trim().toLowerCase(Locale.ROOT);

        String[] codes = language.split("[,;/\\s]+");

        for (String code : codes) {

            code = code.trim();

            if (code.isEmpty()) {
                continue;
            }

            if (code.equals("vi")
                    || code.equals("vie")
                    || code.equals("vietnamese")) {

                return true;
            }

            if (code.contains("-") || code.contains("_")) {

                String[] parts = code.split("[-_]");

                if (parts.length > 0) {
                    code = parts[0];
                }
            }

            if (!isKnownNonVietnameseLanguage(code)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKnownNonVietnameseLanguage(String code) {

        switch (code) {

            case "en":
            case "eng":
            case "english":
            case "fr":
            case "fre":
            case "fra":
            case "de":
            case "ger":
            case "deu":
            case "es":
            case "spa":
            case "it":
            case "ita":
            case "ja":
            case "jpn":
            case "zh":
            case "chi":
            case "zho":
            case "ko":
            case "kor":
            case "ru":
            case "rus":
            case "pt":
            case "por":
            case "ar":
            case "ara":
            case "nl":
            case "nld":
            case "dut":
            case "sv":
            case "swe":
            case "no":
            case "nor":
            case "da":
            case "dan":
            case "fi":
            case "fin":
            case "pl":
            case "pol":
            case "tr":
            case "tur":
            case "hi":
            case "hin":
            case "th":
            case "tha":
                return true;

            default:
                return false;
        }
    }

    // =========================================
    // 16. HÀM HỖ TRỢ
    // =========================================

    private String buildErrorMessage(
            List<String> warnings,
            String defaultMessage
    ) {

        if (warnings == null || warnings.isEmpty()) {
            return defaultMessage;
        }

        StringBuilder builder =
                new StringBuilder(defaultMessage);

        for (String warning : warnings) {

            builder.append("\n- ")
                    .append(warning);
        }

        return builder.toString();
    }

    private String safeMessage(String message) {

        if (message == null || message.trim().isEmpty()) {
            return "Lỗi không xác định";
        }

        return message;
    }

    private String getErrorMessage(Exception e) {

        if (e == null || e.getMessage() == null) {
            return "Lỗi không xác định";
        }

        return e.getMessage();
    }
}