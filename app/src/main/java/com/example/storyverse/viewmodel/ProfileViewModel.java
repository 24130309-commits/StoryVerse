package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.UserEntity;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<UserEntity> user =
            new MutableLiveData<>();

    public LiveData<UserEntity> getUser() {
        return user;
    }

    public void setUser(UserEntity userEntity) {
        user.setValue(userEntity);
    }
}