package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;

public class ReaderViewModel extends ViewModel {

    private final MutableLiveData<ChapterEntity> currentChapter =
            new MutableLiveData<>();

    private final MutableLiveData<ReadingProgressEntity> readingProgress =
            new MutableLiveData<>();

    public LiveData<ChapterEntity> getCurrentChapter() {
        return currentChapter;
    }

    public LiveData<ReadingProgressEntity> getReadingProgress() {
        return readingProgress;
    }

    public void setCurrentChapter(ChapterEntity chapter) {
        currentChapter.setValue(chapter);
    }

    public void setReadingProgress(
            ReadingProgressEntity progress
    ) {
        readingProgress.setValue(progress);
    }
}