package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

@Dao
public interface StoryDao {

    // =========================================
    // CẬP NHẬT TRẠNG THÁI TỪ FIRESTORE
    // =========================================

    @Query(
            "UPDATE stories " +
                    "SET status = :status, updatedAt = :updatedAt " +
                    "WHERE storyId = :storyId"
    )
    void updateStoryStatusFromFirestore(
            String storyId,
            String status,
            long updatedAt
    );

    // =========================================
    // INSERT - THÊM TRUYỆN
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStory(StoryEntity story);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStories(List<StoryEntity> stories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertStoryIfNotExists(StoryEntity story);

    // =========================================
    // CẬP NHẬT METADATA TỪ API
    // Không ghi đè dữ liệu bằng chuỗi rỗng
    // =========================================

    @Query(
            "UPDATE stories SET " +
                    "title = CASE WHEN :title IS NOT NULL AND TRIM(:title) != '' THEN :title ELSE title END, " +
                    "description = CASE WHEN :description IS NOT NULL AND TRIM(:description) != '' THEN :description ELSE description END, " +
                    "coverUrl = CASE WHEN :coverUrl IS NOT NULL AND TRIM(:coverUrl) != '' THEN :coverUrl ELSE coverUrl END, " +
                    "authorId = CASE WHEN :authorId IS NOT NULL AND TRIM(:authorId) != '' THEN :authorId ELSE authorId END, " +
                    "language = CASE WHEN :language IS NOT NULL AND TRIM(:language) != '' THEN :language ELSE language END, " +
                    "source = CASE WHEN :source IS NOT NULL AND TRIM(:source) != '' THEN :source ELSE source END, " +
                    "sourceUrl = CASE WHEN :sourceUrl IS NOT NULL AND TRIM(:sourceUrl) != '' THEN :sourceUrl ELSE sourceUrl END, " +
                    "license = CASE WHEN :license IS NOT NULL AND TRIM(:license) != '' THEN :license ELSE license END, " +
                    "updatedAt = :updatedAt " +
                    "WHERE storyId = :storyId"
    )
    void updateImportedStory(
            String storyId,
            String title,
            String description,
            String coverUrl,
            String authorId,
            String language,
            String source,
            String sourceUrl,
            String license,
            long updatedAt
    );

    // =========================================
    // SELECT - TẤT CẢ TRUYỆN
    // =========================================

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    List<StoryEntity> getAllStories();

    // =========================================
    // SELECT - TRUYỆN THEO ID
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE storyId = :storyId LIMIT 1"
    )
    StoryEntity getStoryById(String storyId);

    @Query(
            "SELECT * FROM stories " +
                    "WHERE storyId = :storyId " +
                    "AND status = 'published' LIMIT 1"
    )
    StoryEntity getPublishedStoryById(String storyId);

    // =========================================
    // SEARCH - TÌM KIẾM TRUYỆN
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE title LIKE '%' || :keyword || '%' " +
                    "ORDER BY title ASC"
    )
    List<StoryEntity> searchStories(String keyword);

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "AND (" +
                    "title LIKE '%' || :keyword || '%' " +
                    "OR description LIKE '%' || :keyword || '%') " +
                    "ORDER BY title ASC"
    )
    List<StoryEntity> searchPublishedStories(String keyword);

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "AND (" +
                    "title LIKE '%' || :keyword || '%' " +
                    "OR description LIKE '%' || :keyword || '%') " +
                    "ORDER BY title COLLATE NOCASE ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<StoryEntity> searchPublishedStoriesPaged(
            String keyword,
            int limit,
            int offset
    );

    // =========================================
    // SELECT - THEO TRẠNG THÁI
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = :status " +
                    "ORDER BY updatedAt DESC"
    )
    List<StoryEntity> getStoriesByStatus(String status);

    // =========================================
    // SELECT - TRUYỆN CÔNG KHAI
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "ORDER BY createdAt DESC"
    )
    List<StoryEntity> getPublishedStories();

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "ORDER BY createdAt DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<StoryEntity> getPublishedStoriesPaged(
            int limit,
            int offset
    );

    // =========================================
    // SELECT - THEO THỂ LOẠI
    // =========================================

    @Query(
            "SELECT s.* FROM stories AS s " +
                    "INNER JOIN story_categories AS sc " +
                    "ON s.storyId = sc.storyId " +
                    "WHERE s.status = 'published' " +
                    "AND sc.categoryId = :categoryId " +
                    "ORDER BY s.updatedAt DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<StoryEntity> getPublishedStoriesByCategory(
            String categoryId,
            int limit,
            int offset
    );

    // =========================================
    // SELECT - THEO TÁC GIẢ
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "AND authorId = :authorId " +
                    "ORDER BY createdAt DESC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<StoryEntity> getPublishedStoriesByAuthor(
            String authorId,
            int limit,
            int offset
    );

    // =========================================
    // SELECT - TRUYỆN MỚI CẬP NHẬT
    // =========================================

    @Query(
            "SELECT * FROM stories " +
                    "WHERE status = 'published' " +
                    "ORDER BY updatedAt DESC"
    )
    List<StoryEntity> getRecentlyUpdatedPublishedStories();

    // =========================================
    // COUNT - THỐNG KÊ
    // =========================================

    @Query("SELECT COUNT(*) FROM stories")
    int getCachedStoryCount();

    @Query(
            "SELECT COUNT(*) FROM stories " +
                    "WHERE status = 'published'"
    )
    int getPublishedStoryCount();

    @Query(
            "SELECT COUNT(*) FROM stories " +
                    "WHERE status = 'pending'"
    )
    int getPendingStoryCount();

    // =========================================
    // DELETE - XÓA TRUYỆN
    // =========================================

    @Delete
    void deleteStory(StoryEntity story);

    @Query("DELETE FROM stories")
    void deleteAllStories();
}