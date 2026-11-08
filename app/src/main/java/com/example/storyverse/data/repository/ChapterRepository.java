package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.ChapterDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.ChapterEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChapterRepository {

    private final ChapterDao chapterDao;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public ChapterRepository(Context context) {

        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        chapterDao = database.chapterDao();
    }

    // =========================
    // Insert
    // =========================

    public void insertChapter(
            ChapterEntity chapter,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            chapterDao.insertChapter(chapter);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void insertChapters(
            List<ChapterEntity> chapters,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            chapterDao.insertChapters(chapters);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // =========================
    // Get chapters
    // =========================

    public void getChaptersByStoryId(
            String storyId,
            RepositoryCallback<List<ChapterEntity>> callback
    ) {

        executorService.execute(() -> {

            try {

                List<ChapterEntity> result =
                        chapterDao.getChaptersByStoryId(storyId);

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void getChapterById(
            String chapterId,
            RepositoryCallback<ChapterEntity> callback
    ) {

        executorService.execute(() -> {

            try {

                ChapterEntity result =
                        chapterDao.getChapterById(chapterId);

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    public void getChapterByNumber(
            String storyId,
            int chapterNumber,
            RepositoryCallback<ChapterEntity> callback
    ) {

        executorService.execute(() -> {

            try {

                ChapterEntity result =
                        chapterDao.getChapterByNumber(
                                storyId,
                                chapterNumber
                        );

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(e);
            }
        });
    }

    // =========================
    // Delete
    // =========================

    public void deleteChapter(
            ChapterEntity chapter,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            chapterDao.deleteChapter(chapter);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteChaptersByStoryId(
            String storyId,
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            chapterDao.deleteChaptersByStoryId(storyId);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deleteAllChapters(
            Runnable onComplete
    ) {

        executorService.execute(() -> {

            chapterDao.deleteAllChapters();

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    // =========================
    // Callback
    // =========================

    public interface RepositoryCallback<T> {

        void onSuccess(T result);

        void onError(Exception e);
    }
}