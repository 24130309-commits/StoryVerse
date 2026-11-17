package com.example.storyverse.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
        version = 5,
        exportSchema = false
)
public abstract class StoryVerseDatabase extends RoomDatabase {

    private static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "ALTER TABLE stories ADD COLUMN chapterCount INTEGER NOT NULL DEFAULT 0"
                    );

                    database.execSQL(
                            "ALTER TABLE stories ADD COLUMN previewChapterCount INTEGER NOT NULL DEFAULT 0"
                    );

                    database.execSQL(
                            "ALTER TABLE stories ADD COLUMN source TEXT"
                    );

                    database.execSQL(
                            "ALTER TABLE stories ADD COLUMN sourceUrl TEXT"
                    );

                    database.execSQL(
                            "ALTER TABLE stories ADD COLUMN license TEXT"
                    );
                }
            };

    private static final Migration MIGRATION_2_3 =
            new Migration(2, 3) {
                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS story_categories_new (" +
                                    "storyId TEXT NOT NULL, " +
                                    "categoryId TEXT NOT NULL, " +
                                    "PRIMARY KEY(storyId, categoryId), " +
                                    "FOREIGN KEY(storyId) REFERENCES stories(storyId) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE, " +
                                    "FOREIGN KEY(categoryId) REFERENCES categories(categoryId) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE" +
                                    ")"
                    );

                    database.execSQL(
                            "INSERT OR IGNORE INTO story_categories_new " +
                                    "(storyId, categoryId) " +
                                    "SELECT sc.storyId, sc.categoryId " +
                                    "FROM story_categories sc " +
                                    "WHERE EXISTS (" +
                                    "SELECT 1 FROM stories s WHERE s.storyId = sc.storyId" +
                                    ") AND EXISTS (" +
                                    "SELECT 1 FROM categories c WHERE c.categoryId = sc.categoryId" +
                                    ")"
                    );

                    database.execSQL(
                            "DROP TABLE story_categories"
                    );

                    database.execSQL(
                            "ALTER TABLE story_categories_new RENAME TO story_categories"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS index_story_categories_categoryId " +
                                    "ON story_categories(categoryId)"
                    );
                }
            };

    // Version 3 -> 4: bổ sung index
    private static final Migration MIGRATION_3_4 =
            new Migration(3, 4) {
                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS index_chapters_storyId_chapterNumber " +
                                    "ON chapters(storyId, chapterNumber)"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS index_stories_status_createdAt " +
                                    "ON stories(status, createdAt)"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS index_stories_status_updatedAt " +
                                    "ON stories(status, updatedAt)"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS index_stories_authorId " +
                                    "ON stories(authorId)"
                    );
                }
            };

    // Version 4 -> 5: hỗ trợ chương có nội dung lớn
    private static final Migration MIGRATION_4_5 =
            new Migration(4, 5) {
                @Override
                public void migrate(
                        @NonNull SupportSQLiteDatabase database
                ) {
                    database.execSQL(
                            "ALTER TABLE chapters ADD COLUMN contentStoragePath TEXT"
                    );
                }
            };

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
                            )
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}