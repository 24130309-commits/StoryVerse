package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.List;

@Dao
public interface ChapterDao {

    // =========================================
    // INSERT - THÊM CHƯƠNG
    // =========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(ChapterEntity chapter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<ChapterEntity> chapters);

    // =========================================
    // CACHE - KIỂM TRA CHƯƠNG ĐÃ LƯU
    // =========================================

    @Query(
            "SELECT EXISTS(" +
                    "SELECT 1 FROM chapters WHERE chapterId = :chapterId)"
    )
    boolean isChapterCached(String chapterId);

    // =========================================
    // UPDATE - CẬP NHẬT CHƯƠNG
    // =========================================

    @Update
    void updateChapter(ChapterEntity chapter);

    @Query(
            "UPDATE chapters SET " +
                    "content = :content, " +
                    "wordCount = :wordCount, " +
                    "updatedAt = :updatedAt " +
                    "WHERE chapterId = :chapterId"
    )
    void updateChapterContent(
            String chapterId,
            String content,
            int wordCount,
            long updatedAt
    );

    // =========================================
    // SELECT - DANH SÁCH CHƯƠNG
    // =========================================

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "ORDER BY chapterNumber ASC"
    )
    List<ChapterEntity> getChaptersByStoryId(String storyId);

    // Lấy chương theo trang
    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "ORDER BY chapterNumber ASC " +
                    "LIMIT :limit OFFSET :offset"
    )
    List<ChapterEntity> getChaptersByStoryIdPaged(
            String storyId,
            int limit,
            int offset
    );

    // Chương đầu tiên
    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "ORDER BY chapterNumber ASC LIMIT 1"
    )
    ChapterEntity getFirstChapter(String storyId);

    // Chương cuối cùng
    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "ORDER BY chapterNumber DESC LIMIT 1"
    )
    ChapterEntity getLastChapter(String storyId);

    // =========================================
    // SELECT - CHI TIẾT CHƯƠNG
    // =========================================

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE chapterId = :chapterId LIMIT 1"
    )
    ChapterEntity getChapterById(String chapterId);

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "AND chapterNumber = :chapterNumber LIMIT 1"
    )
    ChapterEntity getChapterByNumber(
            String storyId,
            int chapterNumber
    );

    // =========================================
    // READER - CHƯƠNG TIẾP THEO
    // =========================================

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "AND chapterNumber > :currentChapterNumber " +
                    "ORDER BY chapterNumber ASC LIMIT 1"
    )
    ChapterEntity getNextChapter(
            String storyId,
            int currentChapterNumber
    );

    // =========================================
    // READER - CHƯƠNG TRƯỚC
    // =========================================

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "AND chapterNumber < :currentChapterNumber " +
                    "ORDER BY chapterNumber DESC LIMIT 1"
    )
    ChapterEntity getPreviousChapter(
            String storyId,
            int currentChapterNumber
    );

    // =========================================
    // SEARCH - TÌM KIẾM CHƯƠNG
    // =========================================

    @Query(
            "SELECT * FROM chapters " +
                    "WHERE storyId = :storyId " +
                    "AND title LIKE '%' || :keyword || '%' " +
                    "ORDER BY chapterNumber ASC"
    )
    List<ChapterEntity> searchChapters(
            String storyId,
            String keyword
    );

    // =========================================
    // COUNT - ĐẾM SỐ CHƯƠNG
    // =========================================

    @Query(
            "SELECT COUNT(*) FROM chapters " +
                    "WHERE storyId = :storyId"
    )
    int getChapterCount(String storyId);

    // Giữ tương thích với ChapterRepository cũ
    @Query(
            "SELECT COUNT(*) FROM chapters " +
                    "WHERE storyId = :storyId"
    )
    int getChapterCountByStoryId(String storyId);

    // Đếm tổng số chương
    @Query("SELECT COUNT(*) FROM chapters")
    int getTotalChapterCount();

    // =========================================
    // DELETE - XÓA CHƯƠNG
    // =========================================

    @Delete
    void deleteChapter(ChapterEntity chapter);

    @Query(
            "DELETE FROM chapters " +
                    "WHERE chapterId = :chapterId"
    )
    void deleteChapterById(String chapterId);

    @Query(
            "DELETE FROM chapters " +
                    "WHERE storyId = :storyId"
    )
    void deleteChaptersByStoryId(String storyId);

    @Query("DELETE FROM chapters")
    void deleteAllChapters();
}