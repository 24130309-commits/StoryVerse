package com.example.storyverse.ui.community;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.CommentAdapter;
import com.example.storyverse.data.local.entity.CommentEntity;
import com.example.storyverse.data.repository.CommentRepository;
import com.example.storyverse.data.remote.firebase.CommentFirestoreDataSource;
import com.example.storyverse.data.remote.firebase.FirestoreManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.UUID;

public class CommunityFragment extends Fragment {

    private CommentRepository commentRepository;
    private CommentAdapter commentAdapter;
    private RecyclerView rvComments;
    private TextView tvEmptyComments;
    private EditText etComment;
    private ListenerRegistration commentsListener;
    private CommentFirestoreDataSource firestoreDataSource;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvComments = view.findViewById(R.id.rvComments);
        tvEmptyComments = view.findViewById(R.id.tvEmptyComments);
        etComment = view.findViewById(R.id.etComment);
        Button btnSend = view.findViewById(R.id.btnSendComment);

        commentRepository = new CommentRepository(requireContext());
        firestoreDataSource = new CommentFirestoreDataSource();
        commentAdapter = new CommentAdapter();

        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(commentAdapter);

        btnSend.setOnClickListener(v -> sendComment());
        loadComments();
    }

    private void sendComment() {
        if (etComment == null) return;

        String content = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            etComment.setError("Vui lòng nhập nội dung bình luận");
            etComment.requestFocus();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Vui lòng đăng nhập để bình luận",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        String displayName = currentUser.getDisplayName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = currentUser.getEmail();
        }

        CommentEntity comment = new CommentEntity(
                UUID.randomUUID().toString(),
                displayName == null ? currentUser.getUid() : displayName,
                null,
                null,
                content,
                null,
                0,
                "active",
                now,
                now
        );

        firestoreDataSource.addComment(comment,
                new FirestoreManager.FirestoreCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        commentRepository.insertCommentAsync(comment, () -> {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                etComment.setText("");
                                Toast.makeText(requireContext(),
                                        "Đã gửi bình luận",
                                        Toast.LENGTH_SHORT).show();
                            });
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Gửi bình luận thất bại: " + error.getMessage(),
                                        Toast.LENGTH_LONG).show());
                    }
                });
    }

    private void loadComments() {
        if (commentRepository == null) return;

        commentRepository.getAllRootCommentsAsync(comments -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> updateCommentList(comments));
            }
        });

        if (commentsListener == null) {
            commentsListener = firestoreDataSource.observeRootComments(
                    new CommentFirestoreDataSource.CommentsCallback() {
                        @Override
                        public void onLoaded(List<CommentEntity> comments) {
                            if (!isAdded()) return;
                            commentRepository.insertComments(comments);
                            requireActivity().runOnUiThread(() -> updateCommentList(comments));
                        }

                        @Override
                        public void onError(Exception error) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(),
                                                "Không tải được bình luận: " + error.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
            );
        }
    }

    private void updateCommentList(List<CommentEntity> comments) {
        commentAdapter.setComments(comments);
        boolean empty = comments == null || comments.isEmpty();
        rvComments.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyComments.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
        super.onDestroyView();
    }
}