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

        chapterDao = database.chapterDao();
    }

        chapterDao.insertChapter(chapter);
    }

        chapterDao.insertChapters(chapters);
    }

    }

    }

            String storyId,
    ) {
                storyId,
                chapterNumber
        );
    }

        chapterDao.deleteChapter(chapter);
    }

        chapterDao.deleteChaptersByStoryId(storyId);
    }

        chapterDao.deleteAllChapters();
    }
}