package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.UserDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.UserEntity;

import java.util.List;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        userDao = database.userDao();
    }

    public void insertUser(UserEntity user) {
        userDao.insertUser(user);
    }

    public void insertUsers(List<UserEntity> users) {
        userDao.insertUsers(users);
    }

    public UserEntity getUserById(String userId) {
        return userDao.getUserById(userId);
    }

    public UserEntity getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public List<UserEntity> getAllUsers() {
        return userDao.getAllUsers();
    }

    public void deleteUser(UserEntity user) {
        userDao.deleteUser(user);
    }

    public void deleteUserById(String userId) {
        userDao.deleteUserById(userId);
    }

    public void deleteAllUsers() {
        userDao.deleteAllUsers();
    }
}