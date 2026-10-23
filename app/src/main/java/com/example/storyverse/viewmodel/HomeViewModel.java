package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<StoryEntity>> stories =
            new MutableLiveData<>();

    public LiveData<List<StoryEntity>> getStories() {
        return stories;
    }

    public void setStories(List<StoryEntity> storyList) {
        stories.setValue(storyList);
    }
}