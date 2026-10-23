package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.CommentDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.List;

public class CommentRepository {

    private final CommentDao commentDao;

    public CommentRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        commentDao = database.commentDao();
    }

    public void insertComment(CommentEntity comment) {
        commentDao.insertComment(comment);
    }

    public void insertComments(List<CommentEntity> comments) {
        commentDao.insertComments(comments);
    }

    public List<CommentEntity> getCommentsByStoryId(
            String storyId
    ) {
        return commentDao.getCommentsByStoryId(storyId);
    }

    public List<CommentEntity> getCommentsByChapterId(
            String chapterId
    ) {
        return commentDao.getCommentsByChapterId(chapterId);
    }

    public List<CommentEntity> getReplies(
            String parentCommentId
    ) {
        return commentDao.getReplies(parentCommentId);
    }

    public CommentEntity getCommentById(String commentId) {
        return commentDao.getCommentById(commentId);
    }

    public void deleteComment(CommentEntity comment) {
        commentDao.deleteComment(comment);
    }

    public void deleteCommentById(String commentId) {
        commentDao.deleteCommentById(commentId);
    }

    public void deleteAllComments() {
        commentDao.deleteAllComments();
    }
}