package com.example.storyverse.data.repository;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.storyverse.data.local.dao.SearchHistoryDao;
import com.example.storyverse.data.local.database.HomeSearchDatabase;
import com.example.storyverse.data.local.database.SearchHistoryDatabaseHelper;
import com.example.storyverse.data.local.entity.SearchHistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchHistoryRepository {

    private SearchHistoryDatabaseHelper databaseHelper;
    private final SearchHistoryDao searchHistoryDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SearchHistoryRepository(Context context, SearchHistoryDao searchHistoryDao) {
        databaseHelper =
                new SearchHistoryDatabaseHelper(
                        context.getApplicationContext()
                );
        this.searchHistoryDao = searchHistoryDao;
    }


    public SearchHistoryRepository(Application application) {
        this.databaseHelper = databaseHelper;
        HomeSearchDatabase db = HomeSearchDatabase.getInstance(application);
        searchHistoryDao = db.searchHistoryDao();
    }

    public LiveData<List<SearchHistoryEntity>> getRecentSearches() {
        return searchHistoryDao.getRecentSearches();
    }

    public void saveKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        String cleanKeyword = keyword.trim();
        executorService.execute(() -> {
            searchHistoryDao.deleteByKeyword(cleanKeyword);
            searchHistoryDao.insert(new SearchHistoryEntity(cleanKeyword, System.currentTimeMillis()));
        });
    }

    public void clearHistory() {
        executorService.execute(searchHistoryDao::clearAll);
    }

}