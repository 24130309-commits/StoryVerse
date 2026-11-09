
package com.example.storyverse.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.List;

@Dao
public interface ChapterDao {

    // Lưu một chương
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(ChapterEntity chapter);

    // Lưu nhiều chương
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<ChapterEntity> chapters);

    // Lấy danh sách chương theo truyện
    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY chapterNumber ASC")
    List<ChapterEntity> getChaptersByStoryId(String storyId);

    // Lấy chương theo ID
    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId LIMIT 1")
    ChapterEntity getChapterById(String chapterId);

    // Lấy chương theo số thứ tự
    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND chapterNumber = :chapterNumber LIMIT 1")
    ChapterEntity getChapterByNumber(String storyId, int chapterNumber);

    // Kiểm tra chương đã được lưu trong Room chưa
    @Query("SELECT EXISTS(SELECT 1 FROM chapters WHERE chapterId = :chapterId)")
    boolean isChapterCached(String chapterId);

    // Đếm số chương đã lưu của một truyện
    @Query("SELECT COUNT(*) FROM chapters WHERE storyId = :storyId")
    int getChapterCountByStoryId(String storyId);

    // Xóa một chương
    @Delete
    void deleteChapter(ChapterEntity chapter);

    // Xóa các chương của một truyện
    @Query("DELETE FROM chapters WHERE storyId = :storyId")
    void deleteChaptersByStoryId(String storyId);

    // Xóa toàn bộ chương
    @Query("DELETE FROM chapters")
    void deleteAllChapters();
}
