package com.example.storyverse.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.storyverse.data.local.dao.SearchHistoryDao;
import com.example.storyverse.data.local.entity.SearchHistoryEntity;

@Database(entities = {SearchHistoryEntity.class}, version = 1, exportSchema = false)
public abstract class HomeSearchDatabase extends RoomDatabase {

    private static volatile HomeSearchDatabase INSTANCE;
    public abstract SearchHistoryDao searchHistoryDao();

    public static HomeSearchDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (HomeSearchDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            HomeSearchDatabase.class,
                            "home_search_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}