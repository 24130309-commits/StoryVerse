
package com.example.storyverse.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.storyverse.data.local.dao.AuthorDao;
import com.example.storyverse.data.local.dao.CategoryDao;
import com.example.storyverse.data.local.dao.StoryCategoryDao;
import com.example.storyverse.data.local.dao.StoryDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.mapper.StoryLocalMapper;
import com.example.storyverse.data.mapper.GutendexMapper;
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.remote.StoryFirestoreDataSource;
import com.example.storyverse.data.remote.firebase.FirestoreManager;
import com.example.storyverse.data.remote.api.model.GutendexResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryRepository {

    private static final String TAG = "StoryRepository";

    private final Context appContext;
    private final StoryVerseDatabase database;

    private final StoryDao storyDao;
    private final AuthorDao authorDao;
    private final CategoryDao categoryDao;
    private final StoryCategoryDao storyCategoryDao;

    private final StoryFirestoreDataSource firestoreDataSource;

    private final MultiSourceStoryRepository multiSourceStoryRepository;
    private final GutendexRepository gutendexRepository;

    private final ExecutorService executorService;
    private final Handler mainHandler;

    public StoryRepository(Context context) {

        appContext = context.getApplicationContext();
        database = StoryVerseDatabase.getInstance(appContext);

        storyDao = database.storyDao();
        authorDao = database.authorDao();
        categoryDao = database.categoryDao();
        storyCategoryDao = database.storyCategoryDao();

        firestoreDataSource = new StoryFirestoreDataSource();

        multiSourceStoryRepository = new MultiSourceStoryRepository();
        gutendexRepository = new GutendexRepository();

        executorService = Executors.newSingleThreadExecutor();

        mainHandler = new Handler(Looper.getMainLooper());
        seedDefaultGenres();
    }

    // =====================================================
    // DANH MỤC THỂ LOẠI MẶC ĐỊNH
    // =====================================================

    private void seedDefaultGenres() {
        executorService.execute(() -> {
            try {
                List<CategoryEntity> defaults = new ArrayList<>();
                String[] names = {
                        "Tiên hiệp", "Kiếm hiệp", "Huyền huyễn", "Dị giới",
                        "Phiêu lưu", "Hành động", "Đô thị", "Học đường",
                        "Thanh xuân", "Ngôn tình", "Đam mỹ", "Xuyên không",
                        "Trọng sinh", "Trinh thám", "Kinh dị", "Tâm lý",
                        "Khoa huyễn", "Quân sự", "Lịch sử", "Cung đấu", "Hài hước"
                };
                for (String name : names) {
                    CategoryEntity category = StoryLocalMapper.toCategoryEntity(name);
                    if (category != null) defaults.add(category);
                }
                categoryDao.insertCategoriesIfNotExists(defaults);
            } catch (Exception e) {
                Log.e(TAG, "Không thể khởi tạo danh mục mặc định", e);
            }
        });
    }

    // =====================================================
    // 1. ROOM - CRUD
    // =====================================================

    public void insertStory(StoryEntity story) {

        executorService.execute(() ->
                storyDao.insertStory(story)
        );
    }

    public void insertStories(List<StoryEntity> stories) {

        if (stories == null || stories.isEmpty()) {
            return;
        }

        executorService.execute(() ->
                storyDao.insertStories(stories)
        );
    }

    public List<StoryEntity> getAllStories() {
        return storyDao.getAllStories();
    }

    public StoryEntity getStoryById(String storyId) {
        return storyDao.getStoryById(storyId);
    }

    public List<StoryEntity> searchStories(String keyword) {
        return storyDao.searchStories(keyword);
    }

    public List<StoryEntity> getStoriesByStatus(String status) {
        return storyDao.getStoriesByStatus(status);
    }

    public void deleteStory(StoryEntity story) {

        executorService.execute(() ->
                storyDao.deleteStory(story)
        );
    }

    public void deleteAllStories() {

        executorService.execute(storyDao::deleteAllStories);
    }

    public void getCachedStoryCount(
            RepositoryCallback<Integer> callback
    ) {

        executorService.execute(() -> {

            try {

                int count = storyDao.getCachedStoryCount();

                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(count);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 2. API -> ROOM
    // =====================================================

    public void saveImportedStory(
            Story story,
            RepositoryCallback<StoryEntity> callback
    ) {

        if (!isValidStory(story)) {

            postError(
                    callback,
                    new IllegalArgumentException(
                            "Dữ liệu truyện không hợp lệ"
                    )
            );

            return;
        }

        executorService.execute(() -> {

            try {

                StoryEntity storyEntity =
                        StoryLocalMapper.toStoryEntity(story);

                if (storyEntity == null) {
                    throw new IllegalArgumentException(
                            "Không thể chuyển dữ liệu truyện"
                    );
                }

                AuthorEntity author =
                        StoryLocalMapper.toAuthorEntity(
                                story.getAuthor()
                        );

                List<CategoryEntity> categories =
                        StoryLocalMapper.toCategoryEntities(story);

                List<StoryCategoryEntity> relations =
                        StoryLocalMapper.toStoryCategoryEntities(story);

                database.runInTransaction(() -> {

                    saveAuthorToRoom(author);

                    saveCategoriesToRoom(categories);

                    storyDao.insertStoryIfNotExists(storyEntity);

                    // Chỉ cập nhật metadata.
                    // Không thay đổi trạng thái duyệt và thống kê.

                    updateStoryMetadata(storyEntity);

                    // Chỉ cập nhật quan hệ thể loại
                    // khi API thực sự cung cấp dữ liệu.

                    if (relations != null && !relations.isEmpty()) {

                        storyCategoryDao.deleteCategoriesByStoryId(
                                storyEntity.getStoryId()
                        );

                        storyCategoryDao.insertStoryCategories(relations);
                    }
                });

                mainHandler.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(storyEntity);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 3. API -> FIRESTORE -> ROOM
    // =====================================================

    public void saveImportedStoryToFirestoreAndRoom(
            Story story,
            RepositoryCallback<StoryEntity> callback
    ) {

        if (!isValidStory(story)) {

            postError(
                    callback,
                    new IllegalArgumentException(
                            "Dữ liệu truyện không hợp lệ"
                    )
            );

            return;
        }

        try {

            StoryEntity storyEntity =
                    StoryLocalMapper.toStoryEntity(story);

            if (storyEntity == null) {

                postError(
                        callback,
                        new IllegalArgumentException(
                                "Không thể chuyển dữ liệu truyện"
                        )
                );

                return;
            }

            AuthorEntity author =
                    StoryLocalMapper.toAuthorEntity(
                            story.getAuthor()
                    );

            List<CategoryEntity> categories =
                    StoryLocalMapper.toCategoryEntities(story);

            List<StoryCategoryEntity> relations =
                    StoryLocalMapper.toStoryCategoryEntities(story);

            firestoreDataSource.saveImportedStory(
                    storyEntity,
                    author,
                    categories,
                    relations,
                    new FirestoreManager.FirestoreCallback<Boolean>() {

                        @Override
                        public void onSuccess(Boolean result) {

                            if (!Boolean.TRUE.equals(result)) {

                                postError(
                                        callback,
                                        new Exception(
                                                "Không thể lưu dữ liệu lên Firestore"
                                        )
                                );

                                return;
                            }

                            // Firestore thành công -> lưu Room.

                            saveImportedStory(
                                    story,
                                    new RepositoryCallback<StoryEntity>() {

                                        @Override
                                        public void onSuccess(
                                                StoryEntity savedStory
                                        ) {

                                            if (callback != null) {
                                                callback.onSuccess(savedStory);
                                            }
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            postError(callback, e);
                                        }
                                    }
                            );
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
    }

    // =====================================================
    // 4. FIRESTORE -> ROOM
    // ĐỒNG BỘ DỮ LIỆU TRUYỆN
    // =====================================================

    public void syncPublishedStoriesFromFirestore(
            RepositoryCallback<Integer> callback
    ) {

        // Giữ nguyên tên hàm để tương thích với code cũ.

        firestoreDataSource.getAllStoriesWithRelations(
                new StoryFirestoreDataSource.SyncCallback() {

                    @Override
                    public void onSuccess(
                            StoryFirestoreDataSource.FirestoreSyncData data
                    ) {

                        executorService.execute(() -> {

                            try {

                                cacheFirestoreData(data);

                                int count = data.stories != null
                                        ? data.stories.size()
                                        : 0;

                                mainHandler.post(() -> {

                                    if (callback != null) {
                                        callback.onSuccess(count);
                                    }
                                });

                            } catch (Exception e) {
                                postError(callback, e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        postError(callback, e);
                    }
                }
        );
    }

    // =====================================================
    // 5. LƯU DỮ LIỆU FIRESTORE VÀO ROOM
    // =====================================================

    private void cacheFirestoreData(
            StoryFirestoreDataSource.FirestoreSyncData data
    ) {

        if (data == null || data.stories == null) {

            throw new IllegalArgumentException(
                    "Dữ liệu đồng bộ Firestore không hợp lệ"
            );
        }

        database.runInTransaction(() -> {

            // Tác giả.

            if (data.authors != null) {

                for (AuthorEntity author : data.authors) {
                    saveAuthorToRoom(author);
                }
            }

            // Thể loại.

            if (data.categories != null) {
                saveCategoriesToRoom(data.categories);
            }

            // Truyện.

            for (StoryEntity story : data.stories) {

                if (story == null
                        || story.getStoryId() == null
                        || story.getStoryId().trim().isEmpty()) {
                    continue;
                }

                storyDao.insertStoryIfNotExists(story);

                updateStoryMetadata(story);

                // Trạng thái duyệt do Firestore quản lý.

                if (story.getStatus() != null
                        && !story.getStatus().trim().isEmpty()) {

                    long updatedAt = story.getUpdatedAt() > 0
                            ? story.getUpdatedAt()
                            : System.currentTimeMillis();

                    storyDao.updateStoryStatusFromFirestore(
                            story.getStoryId(),
                            story.getStatus(),
                            updatedAt
                    );
                }
            }

            // Quan hệ truyện - thể loại.

            if (data.relations != null && !data.relations.isEmpty()) {

                for (StoryEntity story : data.stories) {

                    if (story == null
                            || story.getStoryId() == null
                            || story.getStoryId().trim().isEmpty()) {
                        continue;
                    }

                    storyCategoryDao.deleteCategoriesByStoryId(
                            story.getStoryId()
                    );
                }

                storyCategoryDao.insertStoryCategories(
                        data.relations
                );
            }
        });
    }

    // =====================================================
    // 6. LƯU TÁC GIẢ VÀO ROOM
    // =====================================================

    private void saveAuthorToRoom(AuthorEntity author) {

        if (author == null
                || author.getAuthorId() == null
                || author.getAuthorId().trim().isEmpty()) {
            return;
        }

        authorDao.insertAuthorIfNotExists(author);

        authorDao.updateAuthorIfNotEmpty(
                author.getAuthorId(),
                author.getName(),
                author.getAvatarUrl(),
                author.getBio()
        );
    }

    // =====================================================
    // 7. LƯU THỂ LOẠI VÀO ROOM
    // =====================================================

    private void saveCategoriesToRoom(
            List<CategoryEntity> categories
    ) {

        if (categories == null || categories.isEmpty()) {
            return;
        }

        for (CategoryEntity category : categories) {

            if (category == null
                    || category.getCategoryId() == null
                    || category.getCategoryId().trim().isEmpty()) {
                continue;
            }

            categoryDao.insertCategoryIfNotExists(category);

            categoryDao.updateCategoryIfNotEmpty(
                    category.getCategoryId(),
                    category.getName(),
                    category.getDescription(),
                    category.getIconUrl()
            );
        }
    }

    // =====================================================
    // 8. CẬP NHẬT METADATA TRUYỆN
    // =====================================================

    private void updateStoryMetadata(StoryEntity story) {

        if (story == null || story.getStoryId() == null) {
            return;
        }

        long updatedAt = story.getUpdatedAt() > 0
                ? story.getUpdatedAt()
                : System.currentTimeMillis();

        storyDao.updateImportedStory(
                story.getStoryId(),
                story.getTitle(),
                story.getDescription(),
                story.getCoverUrl(),
                story.getAuthorId(),
                story.getLanguage(),
                story.getSource(),
                story.getSourceUrl(),
                story.getLicense(),
                updatedAt
        );
    }

    // =====================================================
    // 9. FIRESTORE -> ROOM
    // LẤY DANH SÁCH TRUYỆN
    // =====================================================

    public void fetchAndCacheStories(
            RepositoryCallback<List<StoryEntity>> callback
    ) {

        firestoreDataSource.getAllStoriesWithRelations(
                new StoryFirestoreDataSource.SyncCallback() {

                    @Override
                    public void onSuccess(
                            StoryFirestoreDataSource.FirestoreSyncData data
                    ) {

                        executorService.execute(() -> {

                            try {

                                cacheFirestoreData(data);

                                List<StoryEntity> stories =
                                        data.stories != null
                                                ? data.stories
                                                : new ArrayList<>();

                                mainHandler.post(() -> {

                                    if (callback != null) {
                                        callback.onSuccess(stories);
                                    }
                                });

                            } catch (Exception e) {
                                postError(callback, e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        postError(callback, e);
                    }
                }
        );
    }

    // =====================================================
    // 10. ROOM HOẶC FIRESTORE
    // =====================================================

    public void getStoriesFromCacheOrFirestore(
            RepositoryCallback<List<StoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<StoryEntity> cachedStories =
                        storyDao.getAllStories();

                if (cachedStories != null && !cachedStories.isEmpty()) {

                    mainHandler.post(() -> {

                        if (callback != null) {
                            callback.onSuccess(cachedStories);
                        }
                    });

                } else {

                    fetchAndCacheStories(callback);
                }

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 11. FIRESTORE -> ROOM
    // LẤY TRUYỆN THEO ID
    // =====================================================

    public void fetchStoryById(
            String storyId,
            RepositoryCallback<StoryEntity> callback
    ) {

        if (storyId == null || storyId.trim().isEmpty()) {

            postError(
                    callback,
                    new IllegalArgumentException(
                            "ID truyện không hợp lệ"
                    )
            );

            return;
        }

        firestoreDataSource.getStoryById(
                storyId,
                new FirestoreManager.FirestoreCallback<StoryEntity>() {

                    @Override
                    public void onSuccess(StoryEntity story) {

                        if (story == null) {

                            mainHandler.post(() -> {

                                if (callback != null) {
                                    callback.onSuccess(null);
                                }
                            });

                            return;
                        }

                        executorService.execute(() -> {

                            try {

                                storyDao.insertStoryIfNotExists(story);

                                updateStoryMetadata(story);

                                if (story.getStatus() != null
                                        && !story.getStatus().trim().isEmpty()) {

                                    long updatedAt = story.getUpdatedAt() > 0
                                            ? story.getUpdatedAt()
                                            : System.currentTimeMillis();

                                    storyDao.updateStoryStatusFromFirestore(
                                            story.getStoryId(),
                                            story.getStatus(),
                                            updatedAt
                                    );
                                }

                                mainHandler.post(() -> {

                                    if (callback != null) {
                                        callback.onSuccess(story);
                                    }
                                });

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
    }

    // =====================================================
    // 12. NHẬP NHIỀU TRUYỆN
    // API -> FIRESTORE -> ROOM
    // =====================================================

    public interface BulkImportCallback {

        void onComplete(
                int successCount,
                int failedCount,
                List<String> errors
        );
    }

    public void saveImportedStoriesToFirestoreAndRoom(
            List<Story> stories,
            BulkImportCallback callback
    ) {

        if (stories == null || stories.isEmpty()) {

            if (callback != null) {

                mainHandler.post(() ->
                        callback.onComplete(
                                0,
                                0,
                                new ArrayList<>()
                        )
                );
            }

            return;
        }

        Map<String, Story> uniqueStories = new LinkedHashMap<>();

        List<String> invalidErrors = new ArrayList<>();

        for (Story story : stories) {

            if (!isValidStory(story)) {

                invalidErrors.add(
                        "Bỏ qua truyện không có ID hoặc tiêu đề hợp lệ."
                );

                continue;
            }

            uniqueStories.put(story.getStoryId(), story);
        }

        List<Story> importList =
                new ArrayList<>(uniqueStories.values());

        if (importList.isEmpty()) {

            if (callback != null) {

                mainHandler.post(() ->
                        callback.onComplete(
                                0,
                                invalidErrors.size(),
                                invalidErrors
                        )
                );
            }

            return;
        }

        importStoryAtIndex(
                importList,
                0,
                0,
                invalidErrors,
                callback
        );
    }

    private void importStoryAtIndex(
            List<Story> stories,
            int index,
            int successCount,
            List<String> errors,
            BulkImportCallback callback
    ) {

        if (index >= stories.size()) {

            if (callback != null) {

                int finalSuccessCount = successCount;

                int finalFailedCount = errors.size();

                List<String> finalErrors =
                        new ArrayList<>(errors);

                mainHandler.post(() ->
                        callback.onComplete(
                                finalSuccessCount,
                                finalFailedCount,
                                finalErrors
                        )
                );
            }

            return;
        }

        Story story = stories.get(index);

        saveImportedStoryToFirestoreAndRoom(
                story,
                new RepositoryCallback<StoryEntity>() {

                    @Override
                    public void onSuccess(StoryEntity result) {

                        importStoryAtIndex(
                                stories,
                                index + 1,
                                successCount + 1,
                                errors,
                                callback
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        String storyTitle = story.getTitle() == null
                                ? story.getStoryId()
                                : story.getTitle();

                        String errorMessage = e != null
                                ? e.getMessage()
                                : "Lỗi không xác định";

                        errors.add(
                                storyTitle + ": " + errorMessage
                        );

                        Log.e(
                                TAG,
                                "Không thể nhập truyện: " + storyTitle,
                                e
                        );

                        importStoryAtIndex(
                                stories,
                                index + 1,
                                successCount,
                                errors,
                                callback
                        );
                    }
                }
        );
    }

    // =====================================================
    // 13. HOME - TRUYỆN ĐÃ XUẤT BẢN
    // =====================================================

    public void getPublishedStories(
            RepositoryCallback<List<StoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<StoryEntity> stories =
                        storyDao.getPublishedStories();

                mainHandler.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(stories);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // HOME / CATEGORY / AUTHOR - TRUY VẤN PHÂN TRANG
    // =====================================================

    public void getCategories(RepositoryCallback<List<CategoryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<CategoryEntity> result = categoryDao.getAllCategories();
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getCategoriesByStoryId(
            String storyId,
            RepositoryCallback<List<CategoryEntity>> callback
    ) {
        if (storyId == null || storyId.trim().isEmpty()) {
            postError(callback, new IllegalArgumentException("Story ID không hợp lệ"));
            return;
        }
        executorService.execute(() -> {
            try {
                List<CategoryEntity> result =
                        storyCategoryDao.getCategoriesByStoryId(storyId.trim());
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getPublishedStoriesPaged(
            int limit,
            int offset,
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        executorService.execute(() -> {
            try {
                List<StoryEntity> result =
                        storyDao.getPublishedStoriesPaged(safeLimit, safeOffset);
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getPublishedStoriesByCategory(
            String categoryId,
            int limit,
            int offset,
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            postError(callback, new IllegalArgumentException("Category ID không hợp lệ"));
            return;
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        executorService.execute(() -> {
            try {
                List<StoryEntity> result = storyDao.getPublishedStoriesByCategory(
                        categoryId.trim(), safeLimit, safeOffset
                );
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void getPublishedStoriesByAuthor(
            String authorId,
            int limit,
            int offset,
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        if (authorId == null || authorId.trim().isEmpty()) {
            postError(callback, new IllegalArgumentException("Author ID không hợp lệ"));
            return;
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        executorService.execute(() -> {
            try {
                List<StoryEntity> result = storyDao.getPublishedStoriesByAuthor(
                        authorId.trim(), safeLimit, safeOffset
                );
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void searchPublishedStoriesPaged(
            String keyword,
            int limit,
            int offset,
            RepositoryCallback<List<StoryEntity>> callback
    ) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int safeOffset = Math.max(0, offset);
        executorService.execute(() -> {
            try {
                List<StoryEntity> result = storyDao.searchPublishedStoriesPaged(
                        safeKeyword, safeLimit, safeOffset
                );
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(result);
                });
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    // =====================================================
    // 14. SEARCH - TRUYỆN ĐÃ XUẤT BẢN
    // =====================================================

    public void searchPublishedStories(
            String keyword,
            RepositoryCallback<List<StoryEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                String searchKeyword =
                        keyword == null ? "" : keyword.trim();

                List<StoryEntity> stories =
                        storyDao.searchPublishedStories(searchKeyword);

                mainHandler.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(stories);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 15. STORY DETAIL - TRUYỆN ĐÃ XUẤT BẢN
    // =====================================================

    public void getPublishedStoryById(
            String storyId,
            RepositoryCallback<StoryEntity> callback
    ) {

        executorService.execute(() -> {

            try {

                StoryEntity story =
                        storyDao.getPublishedStoryById(storyId);

                mainHandler.post(() -> {

                    if (callback == null) {
                        return;
                    }

                    if (story != null) {
                        callback.onSuccess(story);
                    } else {
                        callback.onError(
                                new Exception(
                                        "Không tìm thấy truyện hoặc truyện chưa được xuất bản."
                                )
                        );
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 16. THỐNG KÊ TRUYỆN ĐÃ XUẤT BẢN
    // =====================================================

    public void getPublishedStoryCount(
            RepositoryCallback<Integer> callback
    ) {

        executorService.execute(() -> {

            try {

                int count = storyDao.getPublishedStoryCount();

                mainHandler.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(count);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 17. THỐNG KÊ TRUYỆN ĐANG CHỜ DUYỆT
    // =====================================================

    public void getPendingStoryCount(
            RepositoryCallback<Integer> callback
    ) {

        executorService.execute(() -> {

            try {

                int count = storyDao.getPendingStoryCount();

                mainHandler.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(count);
                    }
                });

            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    // =====================================================
    // 18. API - TÌM KIẾM TRUYỆN TỪ NHIỀU NGUỒN
    // =====================================================

    public interface ExternalSearchCallback {

        void onSuccess(
                List<Story> stories,
                List<String> warnings
        );

        void onError(String message);
    }

    public void searchExternalStories(
            String keyword,
            ExternalSearchCallback callback
    ) {

        if (callback == null) {
            return;
        }

        multiSourceStoryRepository.searchStories(
                keyword,
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {
                        callback.onSuccess(stories, warnings);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }

    // =====================================================
    // API - TÌM WEBNOVEL NHẬT TỪ NAROU (METADATA + LINK NGUỒN)
    // =====================================================

    public void searchNarouStories(
            String keyword,
            int page,
            int pageSize,
            ExternalSearchCallback callback
    ) {
        if (callback == null) return;
        multiSourceStoryRepository.searchNarouStories(
                keyword,
                page,
                pageSize,
                new MultiSourceStoryRepository.MultiSourceCallback() {
                    @Override
                    public void onSuccess(List<Story> stories, List<String> warnings) {
                        callback.onSuccess(stories, warnings);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }

    // =====================================================
    // 19. API - TÌM SÁCH CÔNG KHAI CÓ THỂ LẤY TOÀN VĂN
    // PROJECT GUTENBERG / GUTENDEX
    // =====================================================

    public void searchPublicDomainStories(
            String keyword,
            int page,
            ExternalSearchCallback callback
    ) {
        if (callback == null) return;
        if (keyword == null || keyword.trim().isEmpty()) {
            callback.onError("Từ khóa tìm kiếm không được để trống");
            return;
        }

        gutendexRepository.searchBooks(
                keyword.trim(),
                page < 1 ? 1 : page,
                new Callback<GutendexResponse>() {
                    @Override
                    public void onResponse(
                            Call<GutendexResponse> call,
                            Response<GutendexResponse> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onError("Gutendex trả về HTTP " + response.code());
                            return;
                        }
                        List<Story> stories = GutendexMapper.toStoryList(
                                response.body().getResults()
                        );
                        if (stories.isEmpty()) {
                            callback.onError(
                                    "Không tìm thấy sách công khai có định dạng toàn văn phù hợp"
                            );
                            return;
                        }
                        callback.onSuccess(stories, new ArrayList<>());
                    }

                    @Override
                    public void onFailure(Call<GutendexResponse> call, Throwable error) {
                        callback.onError(error != null && error.getMessage() != null
                                ? error.getMessage() : "Không thể kết nối Gutendex");
                    }
                }
        );
    }

    // =====================================================
    // 20. API - TÌM KIẾM VỚI SỐ TRANG TÙY CHỈNH
    // =====================================================

    public void searchExternalStories(
            String keyword,
            int pageCount,
            int pageSize,
            ExternalSearchCallback callback
    ) {

        if (callback == null) {
            return;
        }

        multiSourceStoryRepository.searchStories(
                keyword,
                pageCount,
                pageSize,
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {
                        callback.onSuccess(stories, warnings);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }

    // =====================================================
    // NHẬP SÁCH PUBLIC DOMAIN KÈM TOÀN VĂN
    // GUTENDEX -> FIRESTORE + ROOM -> CHAPTER
    // =====================================================

    public void importPublicDomainStoryWithFullText(
            Story story,
            ChapterRepository.RepositoryCallback<ChapterEntity> callback
    ) {
        if (story == null
                || story.getStoryId() == null
                || !story.getStoryId().startsWith("gutendex_")
                || !"Project Gutenberg (Gutendex)".equals(story.getSource())
                || story.getLicense() == null
                || !story.getLicense().startsWith("Public domain")) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError(
                        new IllegalArgumentException(
                                "Chỉ hỗ trợ nhập toàn văn sách Gutendex đã xác nhận public domain."
                        )
                ));
            }
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(
                    story.getStoryId().substring("gutendex_".length())
            );
        } catch (NumberFormatException error) {
            if (callback != null) {
                mainHandler.post(() -> callback.onError(
                        new IllegalArgumentException("Gutendex book ID không hợp lệ", error)
                ));
            }
            return;
        }

        saveImportedStoryToFirestoreAndRoom(
                story,
                new RepositoryCallback<StoryEntity>() {
                    @Override
                    public void onSuccess(StoryEntity savedStory) {
                        ChapterRepository chapterRepository =
                                new ChapterRepository(appContext);
                        chapterRepository.fetchAndSaveGutendexBook(
                                bookId,
                                story.getStoryId(),
                                story.getStoryId() + "_chapter_1",
                                callback
                        );
                    }

                    @Override
                    public void onError(Exception error) {
                        if (callback != null) callback.onError(error);
                    }
                }
        );
    }

    // =====================================================
    // 22. API - NHẬP CÁC TRUYỆN ĐƯỢC CHỌN
    // =====================================================

    public void importExternalStories(
            List<Story> selectedStories,
            BulkImportCallback callback
    ) {

        saveImportedStoriesToFirestoreAndRoom(
                selectedStories,
                callback
        );
    }

    // =====================================================
    // 23. THU THẬP VÀ NHẬP KHO TRUYỆN ĐA THỂ LOẠI
    // API -> FIRESTORE -> ROOM
    // =====================================================

    public void collectAndImportGenreStories(
            BulkImportCallback callback
    ) {

        if (callback == null) {
            return;
        }

        multiSourceStoryRepository.collectGenreStories(
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {

                        if (stories == null || stories.isEmpty()) {

                            List<String> errors = new ArrayList<>();

                            if (warnings != null) {
                                errors.addAll(warnings);
                            }

                            errors.add(
                                    "Không tìm thấy truyện để nhập vào kho."
                            );

                            callback.onComplete(0, 0, errors);

                            return;
                        }

                        Log.i(
                                TAG,
                                "Thu thập được "
                                        + stories.size()
                                        + " truyện sau khi khử trùng."
                        );

                        saveImportedStoriesToFirestoreAndRoom(
                                stories,
                                (successCount, failedCount, errors) -> {

                                    List<String> allErrors =
                                            new ArrayList<>();

                                    if (warnings != null) {
                                        allErrors.addAll(warnings);
                                    }

                                    if (errors != null) {
                                        allErrors.addAll(errors);
                                    }

                                    Log.i(
                                            TAG,
                                            "Kết quả nhập kho: thành công="
                                                    + successCount
                                                    + ", thất bại="
                                                    + failedCount
                                    );

                                    callback.onComplete(
                                            successCount,
                                            failedCount,
                                            allErrors
                                    );
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {

                        List<String> errors = new ArrayList<>();

                        errors.add(
                                "Không thể thu thập kho truyện: "
                                        + message
                        );

                        callback.onComplete(0, 0, errors);
                    }
                }
        );
    }

    // =====================================================
    // 24. XEM TRƯỚC KHO TRUYỆN
    // CHỈ THU THẬP - KHÔNG LƯU
    // =====================================================

    public void previewGenreStories(
            ExternalSearchCallback callback
    ) {

        if (callback == null) {
            return;
        }

        multiSourceStoryRepository.collectGenreStories(
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {

                        List<Story> resultStories =
                                stories != null
                                        ? stories
                                        : new ArrayList<>();

                        List<String> resultWarnings =
                                warnings != null
                                        ? warnings
                                        : new ArrayList<>();

                        Log.i(
                                TAG,
                                "========== PREVIEW KHO TRUYỆN =========="
                        );

                        Log.i(
                                TAG,
                                "Tổng số truyện: "
                                        + resultStories.size()
                        );

                        Log.i(
                                TAG,
                                "Tổng số cảnh báo: "
                                        + resultWarnings.size()
                        );

                        for (String warning : resultWarnings) {
                            Log.w(TAG, warning);
                        }

                        Log.i(
                                TAG,
                                "========== KẾT THÚC PREVIEW =========="
                        );

                        callback.onSuccess(
                                resultStories,
                                resultWarnings
                        );
                    }

                    @Override
                    public void onError(String message) {

                        Log.e(
                                TAG,
                                "Không thể xem trước kho truyện: "
                                        + message
                        );

                        callback.onError(message);
                    }
                }
        );
    }

    // =====================================================
    // 23. KIỂM TRA DỮ LIỆU TRUYỆN
    // =====================================================

    private boolean isValidStory(Story story) {

        return story != null
                && story.getStoryId() != null
                && !story.getStoryId().trim().isEmpty()
                && story.getTitle() != null
                && !story.getTitle().trim().isEmpty();
    }

    // =====================================================
    // 24. ERROR HANDLING
    // =====================================================

    private <T> void postError(
            RepositoryCallback<T> callback,
            Exception e
    ) {

        mainHandler.post(() -> {

            if (callback != null) {
                callback.onError(e);
            }
        });
    }

    // =====================================================
    // 25. CALLBACK
    // =====================================================

    public interface RepositoryCallback<T> {

        void onSuccess(T result);

        void onError(Exception e);
    }
}
