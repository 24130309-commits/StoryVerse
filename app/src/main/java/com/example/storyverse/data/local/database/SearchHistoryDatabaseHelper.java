package com.example.storyverse.data.local.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "storyverse_search.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SEARCH_HISTORY = "search_history";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_KEYWORD = "keyword";
    public static final String COLUMN_SEARCHED_AT = "searchedAt";

    private static final String CREATE_TABLE_SEARCH_HISTORY =
            "CREATE TABLE " + TABLE_SEARCH_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_KEYWORD + " TEXT NOT NULL, " +
                    COLUMN_SEARCHED_AT + " INTEGER NOT NULL" +
                    ")";

    public SearchHistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SEARCH_HISTORY);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        db.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY
        );

        onCreate(db);
    }

    // =========================
    // Search History
    // =========================

    public long insertSearchKeyword(String keyword) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_KEYWORD, keyword);
        values.put(COLUMN_SEARCHED_AT, System.currentTimeMillis());

        return db.insert(
                TABLE_SEARCH_HISTORY,
                null,
                values
        );
    }

    public List<String> getSearchHistory() {
        List<String> history = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_SEARCH_HISTORY,
                new String[]{COLUMN_KEYWORD},
                null,
                null,
                null,
                null,
                COLUMN_SEARCHED_AT + " DESC"
        );

        try {
            while (cursor.moveToNext()) {
                history.add(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(COLUMN_KEYWORD)
                        )
                );
            }
        } finally {
            cursor.close();
        }

        return history;
    }

    public void deleteSearchKeyword(String keyword) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(
                TABLE_SEARCH_HISTORY,
                COLUMN_KEYWORD + " = ?",
                new String[]{keyword}
        );
    }

    public void clearSearchHistory() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(
                TABLE_SEARCH_HISTORY,
                null,
                null
        );
    }
}