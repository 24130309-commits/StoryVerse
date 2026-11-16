
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapter(ChapterEntity chapter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChapters(List<ChapterEntity> chapters);

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY chapterNumber ASC")
    List<ChapterEntity> getChaptersByStoryId(String storyId);

    @Query("SELECT * FROM chapters WHERE chapterId = :chapterId LIMIT 1")
    ChapterEntity getChapterById(String chapterId);

    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND chapterNumber = :chapterNumber LIMIT 1")
    ChapterEntity getChapterByNumber(String storyId, int chapterNumber);

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY chapterNumber ASC LIMIT :limit OFFSET :offset")
    List<ChapterEntity> getChaptersByStoryIdPaged(
            String storyId,
            int limit,
            int offset
    );

    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND chapterNumber > :currentChapterNumber ORDER BY chapterNumber ASC LIMIT 1")
    ChapterEntity getNextChapter(String storyId, int currentChapterNumber);

    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND chapterNumber < :currentChapterNumber ORDER BY chapterNumber DESC LIMIT 1")
    ChapterEntity getPreviousChapter(String storyId, int currentChapterNumber);

    @Query("SELECT EXISTS(SELECT 1 FROM chapters WHERE chapterId = :chapterId)")
    boolean isChapterCached(String chapterId);

    @Query("SELECT COUNT(*) FROM chapters WHERE storyId = :storyId")
    int getChapterCountByStoryId(String storyId);

    @Delete
    void deleteChapter(ChapterEntity chapter);

    @Query("DELETE FROM chapters WHERE storyId = :storyId")
    void deleteChaptersByStoryId(String storyId);

    @Query("DELETE FROM chapters")
    void deleteAllChapters();
}
