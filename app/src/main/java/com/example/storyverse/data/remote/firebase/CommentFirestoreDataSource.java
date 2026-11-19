package com.example.storyverse.data.remote.firebase;

import com.example.storyverse.data.local.entity.CommentEntity;
import com.example.storyverse.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentFirestoreDataSource {

    private final FirebaseFirestore firestore;

    public CommentFirestoreDataSource() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface CommentsCallback {
        void onLoaded(List<CommentEntity> comments);
        void onError(Exception error);
    }

    public ListenerRegistration observeRootComments(CommentsCallback callback) {
        return firestore.collection(Constants.COLLECTION_COMMENTS)
                .whereEqualTo("parentCommentId", null)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        if (callback != null) callback.onError(error);
                        return;
                    }

                    List<CommentEntity> comments = new ArrayList<>();
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : snapshots.getDocuments()) {
                            CommentEntity comment = documentToComment(document);
                            if (comment != null) comments.add(comment);
                        }
                    }

                    if (callback != null) callback.onLoaded(comments);
                });
    }

    public void addComment(CommentEntity comment, FirestoreManager.FirestoreCallback<String> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("commentId", comment.getCommentId());
        data.put("userId", comment.getUserId());
        data.put("storyId", comment.getStoryId());
        data.put("chapterId", comment.getChapterId());
        data.put("content", comment.getContent());
        data.put("parentCommentId", comment.getParentCommentId());
        data.put("likeCount", comment.getLikeCount());
        data.put("status", comment.getStatus());
        data.put("createdAt", comment.getCreatedAt());
        data.put("updatedAt", comment.getUpdatedAt());

        firestore.collection(Constants.COLLECTION_COMMENTS)
                .document(comment.getCommentId())
                .set(data)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(comment.getCommentId());
                })
                .addOnFailureListener(error -> {
                    if (callback != null) callback.onFailure(error);
                });
    }

    private CommentEntity documentToComment(com.google.firebase.firestore.DocumentSnapshot document) {
        CommentEntity comment = new CommentEntity();
        comment.setCommentId(document.getId());
        comment.setUserId(document.getString("userId"));
        comment.setStoryId(document.getString("storyId"));
        comment.setChapterId(document.getString("chapterId"));
        comment.setContent(document.getString("content"));
        comment.setParentCommentId(document.getString("parentCommentId"));
        comment.setStatus(document.getString("status"));

        Long likeCount = document.getLong("likeCount");
        Long createdAt = document.getLong("createdAt");
        Long updatedAt = document.getLong("updatedAt");

        comment.setLikeCount(likeCount == null ? 0 : likeCount);
        comment.setCreatedAt(createdAt == null ? 0 : createdAt);
        comment.setUpdatedAt(updatedAt == null ? 0 : updatedAt);
        return comment;
    }
}
