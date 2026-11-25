package com.example.storyverse.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.NotificationAdapter;
import com.example.storyverse.data.local.entity.NotificationEntity;
import com.example.storyverse.data.remote.firebase.NotificationFirestoreDataSource;
import com.example.storyverse.data.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationFragment extends Fragment {

    private NotificationRepository repository;
    private NotificationAdapter adapter;
    private RecyclerView rvNotifications;
    private TextView tvEmptyNotifications;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ListenerRegistration notificationsListener;
    private NotificationFirestoreDataSource firestoreDataSource;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyNotifications = view.findViewById(R.id.tvEmptyNotifications);
        Button btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        repository = new NotificationRepository(requireContext());
        adapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        firestoreDataSource = new NotificationFirestoreDataSource();

        adapter.setOnNotificationClickListener(notification -> {
            executor.execute(() -> {
                repository.markAsRead(notification.getNotificationId());
                if (isAdded()) loadNotifications();
            });
        });

        btnMarkAllRead.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                executor.execute(() -> {
                    repository.markAllAsRead(user.getUid());
                    if (isAdded()) loadNotifications();
                });
            }
        });

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            updateNotifications(List.of());
            return;
        }

        executor.execute(() -> {
            List<NotificationEntity> cached = repository.getNotificationsByUserId(user.getUid());
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> updateNotifications(cached));
            }
        });

        if (notificationsListener == null) {
            notificationsListener = firestoreDataSource.observeNotifications(
                    user.getUid(),
                    new NotificationFirestoreDataSource.Callback() {
                        @Override
                        public void onLoaded(List<NotificationEntity> notifications) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> updateNotifications(notifications));
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            // Giữ dữ liệu cache Room nếu Firestore lỗi
                        }
                    }
            );
        }
    }

    private void updateNotifications(List<NotificationEntity> notifications) {
        if (!isAdded()) return;

        adapter.setNotifications(notifications);
        boolean empty = notifications == null || notifications.isEmpty();
        rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyNotifications.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
        executor.shutdownNow();
        super.onDestroyView();
    }
}
