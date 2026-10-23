package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<StoryEntity>> searchResults =
            new MutableLiveData<>();

    private final MutableLiveData<String> keyword =
            new MutableLiveData<>("");

    public LiveData<List<StoryEntity>> getSearchResults() {
        return searchResults;
    }

    public LiveData<String> getKeyword() {
        return keyword;
    }

    public void setSearchResults(List<StoryEntity> results) {
        searchResults.setValue(results);
    }

    public void setKeyword(String value) {
        keyword.setValue(value);
    }
}