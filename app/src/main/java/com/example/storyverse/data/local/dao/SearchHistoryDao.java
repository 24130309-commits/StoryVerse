package com.example.storyverse.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.storyverse.data.local.entity.SearchHistoryEntity;
import java.util.List;

@Dao
public interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SearchHistoryEntity history);

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    LiveData<List<SearchHistoryEntity>> getRecentSearches();

    @Query("DELETE FROM search_history WHERE keyword = :keyword")
    void deleteByKeyword(String keyword);

    @Query("DELETE FROM search_history")
    void clearAll();
}