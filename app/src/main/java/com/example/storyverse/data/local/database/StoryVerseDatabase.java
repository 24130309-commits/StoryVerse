package com.example.storyverse.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.storyverse.data.local.dao.AuthorDao;
import com.example.storyverse.data.local.dao.CategoryDao;
import com.example.storyverse.data.local.dao.ChapterDao;
import com.example.storyverse.data.local.dao.CommentDao;
import com.example.storyverse.data.local.dao.FavoriteDao;
import com.example.storyverse.data.local.dao.NotificationDao;
import com.example.storyverse.data.local.dao.ReadingHistoryDao;
import com.example.storyverse.data.local.dao.ReadingProgressDao;
import com.example.storyverse.data.local.dao.StoryCategoryDao;
import com.example.storyverse.data.local.dao.StoryDao;
import com.example.storyverse.data.local.dao.UserDao;

import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.CommentEntity;
import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.NotificationEntity;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.data.local.entity.StoryCategoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.local.entity.UserEntity;

@Database(
        entities = {
                UserEntity.class,
                AuthorEntity.class,
                CategoryEntity.class,
                StoryCategoryEntity.class,
                StoryEntity.class,
                ChapterEntity.class,
                FavoriteEntity.class,
                ReadingProgressEntity.class,
                ReadingHistoryEntity.class,
                CommentEntity.class,
                NotificationEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class StoryVerseDatabase extends RoomDatabase {

    private static volatile StoryVerseDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract AuthorDao authorDao();

    public abstract CategoryDao categoryDao();

    public abstract StoryCategoryDao storyCategoryDao();

    public abstract StoryDao storyDao();

    public abstract ChapterDao chapterDao();

    public abstract FavoriteDao favoriteDao();

    public abstract ReadingProgressDao readingProgressDao();

    public abstract ReadingHistoryDao readingHistoryDao();

    public abstract CommentDao commentDao();

    public abstract NotificationDao notificationDao();

    public static StoryVerseDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (StoryVerseDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            StoryVerseDatabase.class,
                            "storyverse_database"
                    ).build();
                }
            }
        }

        return INSTANCE;
    }
}