package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.database.SearchHistoryDatabaseHelper;

import java.util.List;

public class SearchHistoryRepository {

    private final SearchHistoryDatabaseHelper databaseHelper;

    public SearchHistoryRepository(Context context) {
        databaseHelper =
                new SearchHistoryDatabaseHelper(
                        context.getApplicationContext()
                );
    }

    public long insertSearchKeyword(String keyword) {
        return databaseHelper.insertSearchKeyword(keyword);
    }

    public List<String> getSearchHistory() {
        return databaseHelper.getSearchHistory();
    }

    public void deleteSearchKeyword(String keyword) {
        databaseHelper.deleteSearchKeyword(keyword);
    }

    public void clearSearchHistory() {
        databaseHelper.clearSearchHistory();
    }
}