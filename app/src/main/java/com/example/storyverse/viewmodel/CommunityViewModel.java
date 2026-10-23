package com.example.storyverse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.List;

public class CommunityViewModel extends ViewModel {

    private final MutableLiveData<List<CommentEntity>> comments =
            new MutableLiveData<>();

    public LiveData<List<CommentEntity>> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> commentList) {
        comments.setValue(commentList);
    }
}