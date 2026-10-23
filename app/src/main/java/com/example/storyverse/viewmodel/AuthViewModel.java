package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<Boolean> loginState =
            new MutableLiveData<>(false);

    private final MutableLiveData<String> userId =
            new MutableLiveData<>();

    public LiveData<Boolean> getLoginState() {
        return loginState;
    }

    public LiveData<String> getUserId() {
        return userId;
    }

    public void setLoginState(boolean isLoggedIn) {
        loginState.setValue(isLoggedIn);
    }

    public void setUserId(String id) {
        userId.setValue(id);
    }
}