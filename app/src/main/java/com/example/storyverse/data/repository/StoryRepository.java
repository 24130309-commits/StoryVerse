package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.StoryDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.StoryEntity;

import java.util.List;

public class StoryRepository {

    private final StoryDao storyDao;

    public StoryRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        storyDao = database.storyDao();
    }

    public void insertStory(StoryEntity story) {
        storyDao.insertStory(story);
    }

    public void insertStories(List<StoryEntity> stories) {
        storyDao.insertStories(stories);
    }

    public List<StoryEntity> getAllStories() {
        return storyDao.getAllStories();
    }

    public StoryEntity getStoryById(String storyId) {
        return storyDao.getStoryById(storyId);
    }

    public List<StoryEntity> searchStories(String keyword) {
        return storyDao.searchStories(keyword);
    }

    public List<StoryEntity> getStoriesByStatus(String status) {
        return storyDao.getStoriesByStatus(status);
    }

    public void deleteStory(StoryEntity story) {
        storyDao.deleteStory(story);
    }

    public void deleteAllStories() {
        storyDao.deleteAllStories();
    }
}