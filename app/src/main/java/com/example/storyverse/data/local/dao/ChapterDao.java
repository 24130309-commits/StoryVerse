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

    @Delete
    void deleteChapter(ChapterEntity chapter);

    @Query("DELETE FROM chapters WHERE storyId = :storyId")
    void deleteChaptersByStoryId(String storyId);

    @Query("DELETE FROM chapters")
    void deleteAllChapters();
}