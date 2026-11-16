package com.example.storyverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.FavoriteRepository;
import com.example.storyverse.data.repository.StoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryViewModel extends AndroidViewModel {

    private final FavoriteRepository favoriteRepository;
    private final StoryRepository storyRepository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<StoryEntity>> favoriteStories = new MutableLiveData<>(new ArrayList<>());

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
        storyRepository = new StoryRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<StoryEntity>> getFavoriteStories() {
        return favoriteStories;
    }

    public void loadFavorites(String userId) {
        executorService.execute(() -> {
            List<FavoriteEntity> favorites = favoriteRepository.getFavoritesByUserId(userId);
            List<StoryEntity> stories = new ArrayList<>();
            if (favorites != null) {
                for (FavoriteEntity fav : favorites) {
                    StoryEntity story = storyRepository.getStoryById(fav.getStoryId());
                    if (story != null) {
                        stories.add(story);
                    }
                }
            }
            favoriteStories.postValue(stories);
        });
    }

    public void removeFavorite(String userId, String storyId) {
        executorService.execute(() -> {
            favoriteRepository.deleteFavorite(userId, storyId);
            loadFavorites(userId);
        });
    }
}