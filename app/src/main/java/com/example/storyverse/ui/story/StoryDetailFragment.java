package com.example.storyverse.ui.story;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.FavoriteRepository;
import com.example.storyverse.data.repository.StoryRepository;
import com.example.storyverse.ui.reader.ReaderFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryDetailFragment extends Fragment {

    private static final String ARG_STORY_ID = "storyId";

    private TextView tvTitle, tvAuthor, tvStatus, tvStats, tvDescription;
    private ImageView ivCover;
    private ImageButton btnBack, btnFavorite;
    private Button btnReadNow;

    private StoryRepository storyRepository;
    private FavoriteRepository favoriteRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String storyId;
    private boolean isFav = false;

    public static StoryDetailFragment newInstance(String storyId) {
        StoryDetailFragment fragment = new StoryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STORY_ID, storyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvStoryTitle);
        tvAuthor = view.findViewById(R.id.tvStoryAuthor);
        tvStatus = view.findViewById(R.id.tvStoryStatus);
        tvStats = view.findViewById(R.id.tvStoryStats);
        tvDescription = view.findViewById(R.id.tvStoryDescription);
        ivCover = view.findViewById(R.id.ivStoryCover);
        btnBack = view.findViewById(R.id.btnBack);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        btnReadNow = view.findViewById(R.id.btnReadNow);

        storyRepository = new StoryRepository(requireContext());
        favoriteRepository = new FavoriteRepository(requireContext());

        if (getArguments() != null) {
            storyId = getArguments().getString(ARG_STORY_ID);
        }

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (storyId != null && !storyId.isEmpty()) {
            loadStoryDetail();
            checkFavoriteStatus();
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnReadNow.setOnClickListener(v -> {
            if (storyId == null) return;
            ReaderFragment readerFragment = new ReaderFragment();
            Bundle bundle = new Bundle();
            bundle.putString("storyId", storyId);
            readerFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), readerFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadStoryDetail() {
        executorService.execute(() -> {
            StoryEntity localStory = storyRepository.getStoryById(storyId);
            mainHandler.post(() -> {
                if (!isAdded()) return;
                if (localStory != null) {
                    bindStoryData(localStory);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy dữ liệu truyện", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void bindStoryData(StoryEntity story) {
        tvTitle.setText(story.getTitle() != null ? story.getTitle() : "Chưa có tên");
        tvAuthor.setText("Tác giả: " + (story.getAuthorId() != null ? story.getAuthorId() : "Đang cập nhật"));
        tvStatus.setText(story.getStatus() != null ? story.getStatus() : "Đang phát hành");
        tvStats.setText(story.getChapterCount() + " chương • " + story.getFavoriteCount() + " lượt thích");
        tvDescription.setText(story.getDescription() != null ? story.getDescription() : "Chưa có mô tả cho truyện này.");
    }

    private void checkFavoriteStatus() {
        String userId = getCurrentUserId();
        executorService.execute(() -> {
            boolean favorite = favoriteRepository.isFavorite(userId, storyId);
            mainHandler.post(() -> {
                isFav = favorite;
                updateFavoriteIcon();
            });
        });
    }

    private void toggleFavorite() {
        if (storyId == null) return;
        String userId = getCurrentUserId();

        executorService.execute(() -> {
            if (isFav) {
                favoriteRepository.deleteFavorite(userId, storyId);
                mainHandler.post(() -> {
                    isFav = false;
                    updateFavoriteIcon();
                    Toast.makeText(getContext(), "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                });
            } else {
                FavoriteEntity favorite = new FavoriteEntity(
                        UUID.randomUUID().toString(),
                        userId,
                        storyId,
                        System.currentTimeMillis()
                );
                favoriteRepository.insertFavorite(favorite);
                mainHandler.post(() -> {
                    isFav = true;
                    updateFavoriteIcon();
                    Toast.makeText(getContext(), "Đã lưu vào thư viện", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFav
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "guest_user";
    }
}