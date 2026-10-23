package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.ChapterDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.List;

public class ChapterRepository {

    private final ChapterDao chapterDao;

    public ChapterRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        chapterDao = database.chapterDao();
    }

    public void insertChapter(ChapterEntity chapter) {
        chapterDao.insertChapter(chapter);
    }

    public void insertChapters(List<ChapterEntity> chapters) {
        chapterDao.insertChapters(chapters);
    }

    public List<ChapterEntity> getChaptersByStoryId(String storyId) {
        return chapterDao.getChaptersByStoryId(storyId);
    }

    public ChapterEntity getChapterById(String chapterId) {
        return chapterDao.getChapterById(chapterId);
    }

    public ChapterEntity getChapterByNumber(
            String storyId,
            int chapterNumber
    ) {
        return chapterDao.getChapterByNumber(
                storyId,
                chapterNumber
        );
    }

    public void deleteChapter(ChapterEntity chapter) {
        chapterDao.deleteChapter(chapter);
    }

    public void deleteChaptersByStoryId(String storyId) {
        chapterDao.deleteChaptersByStoryId(storyId);
    }

    public void deleteAllChapters() {
        chapterDao.deleteAllChapters();
    }
}