package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

public class LibraryViewModel extends ViewModel {

    private final MutableLiveData<List<FavoriteEntity>> favorites =
            new MutableLiveData<>();

    private final MutableLiveData<List<StoryEntity>> stories =
            new MutableLiveData<>();

    public LiveData<List<FavoriteEntity>> getFavorites() {
        return favorites;
    }

    public LiveData<List<StoryEntity>> getStories() {
        return stories;
    }

    public void setFavorites(List<FavoriteEntity> favoriteList) {
        favorites.setValue(favoriteList);
    }

    public void setStories(List<StoryEntity> storyList) {
        stories.setValue(storyList);
    }
}