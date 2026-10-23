package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

public class StoryViewModel extends ViewModel {

    private final MutableLiveData<StoryEntity> story =
            new MutableLiveData<>();

    private final MutableLiveData<List<ChapterEntity>> chapters =
            new MutableLiveData<>();

    public LiveData<StoryEntity> getStory() {
        return story;
    }

    public LiveData<List<ChapterEntity>> getChapters() {
        return chapters;
    }

    public void setStory(StoryEntity storyEntity) {
        story.setValue(storyEntity);
    }

    public void setChapters(List<ChapterEntity> chapterList) {
        chapters.setValue(chapterList);
    }
}