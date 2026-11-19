package com.example.storyverse.data.repository;

import android.content.Context;

import com.example.storyverse.data.local.dao.CommentDao;
import com.example.storyverse.data.local.database.StoryVerseDatabase;
import com.example.storyverse.data.local.entity.CommentEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentRepository {

    private final CommentDao commentDao;
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public CommentRepository(Context context) {
        StoryVerseDatabase database =
                StoryVerseDatabase.getInstance(context);

        commentDao = database.commentDao();
    }

    public void insertComment(CommentEntity comment) {
        executorService.execute(() -> commentDao.insertComment(comment));
    }

    public void insertComments(List<CommentEntity> comments) {
        executorService.execute(() -> commentDao.insertComments(comments));
    }

    public void getCommentsByStoryId(
            String storyId,
            CommentsCallback callback
    ) {
        executorService.execute(() -> {
            List<CommentEntity> comments = commentDao.getCommentsByStoryId(storyId);
            callback.onCommentsReceived(comments);
        });
    }

    public void getCommentsByChapterId(
            String chapterId,
            CommentsCallback callback
    ) {
        executorService.execute(() -> {
            List<CommentEntity> comments = commentDao.getCommentsByChapterId(chapterId);
            callback.onCommentsReceived(comments);
        });
    }

    public void getReplies(
            String parentCommentId,
            CommentsCallback callback
    ) {
        executorService.execute(() -> {
            List<CommentEntity> replies = commentDao.getReplies(parentCommentId);
            callback.onCommentsReceived(replies);
        });
    }

    public void getCommentById(
            String commentId,
            CommentCallback callback
    ) {
        executorService.execute(() -> {
            CommentEntity comment = commentDao.getCommentById(commentId);
            callback.onCommentReceived(comment);
        });
    }

    public void deleteComment(CommentEntity comment) {
        executorService.execute(() -> commentDao.deleteComment(comment));
    }

    public void deleteCommentById(String commentId) {
        executorService.execute(() -> commentDao.deleteCommentById(commentId));
    }

    public void deleteAllComments() {
        executorService.execute(() -> commentDao.deleteAllComments());
    }

    public void insertCommentAsync(CommentEntity comment, Runnable onComplete) {
        executorService.execute(() -> {
            commentDao.insertComment(comment);
            if (onComplete != null) onComplete.run();
        });
    }

    public void getAllRootCommentsAsync(java.util.function.Consumer<List<CommentEntity>> callback) {
        executorService.execute(() -> callback.accept(commentDao.getAllRootComments()));
    }

    public interface CommentsCallback {
        void onCommentsReceived(List<CommentEntity> comments);
    }

    public interface CommentCallback {
        void onCommentReceived(CommentEntity comment);
    }
}