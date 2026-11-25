
package com.example.storyverse.ui.story;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.storyverse.R;
import com.example.storyverse.adapter.ChapterAdapter;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.FavoriteEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.ChapterRepository;
import com.example.storyverse.data.repository.FavoriteRepository;
import com.example.storyverse.data.repository.StoryRepository;
import com.example.storyverse.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoryDetailFragment extends Fragment {

    private static final String ARG_STORY_ID = "storyId";
    private static final String ARG_CHAPTER_ID = "chapterId";

    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvStatus;
    private TextView tvStats;
    private TextView tvDescription;
    private TextView tvChapterCount;
    private TextView tvChapterEmpty;
    private TextView tvChapterError;

    private ImageView ivCover;
    private ProgressBar progressCover;
    private ProgressBar progressChapters;

    private ImageButton btnBack;
    private ImageButton btnFavorite;
    private Button btnReadNow;

    private RecyclerView rvChapters;

    private StoryRepository storyRepository;
    private FavoriteRepository favoriteRepository;
    private ChapterRepository chapterRepository;

    private ChapterAdapter chapterAdapter;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private String storyId;

    private boolean isFav = false;
    private boolean isFavoriteRequestRunning = false;

    public static StoryDetailFragment newInstance(String storyId) {
        StoryDetailFragment fragment = new StoryDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_STORY_ID, storyId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_story_detail,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        storyRepository = new StoryRepository(requireContext());
        favoriteRepository = new FavoriteRepository(requireContext());
        chapterRepository = new ChapterRepository(requireContext());

        if (getArguments() != null) {
            storyId = getArguments().getString(ARG_STORY_ID);
        }

        setupChapterList();
        setupListeners(view);

        if (storyId == null || storyId.trim().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Không tìm thấy mã truyện",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        loadStoryDetail();
        loadChapters();
        checkFavoriteStatus();
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tvStoryTitle);
        tvAuthor = view.findViewById(R.id.tvStoryAuthor);
        tvStatus = view.findViewById(R.id.tvStoryStatus);
        tvStats = view.findViewById(R.id.tvStoryStats);
        tvDescription = view.findViewById(R.id.tvStoryDescription);

        tvChapterCount = view.findViewById(R.id.tvChapterCount);
        tvChapterEmpty = view.findViewById(R.id.tvChapterEmpty);
        tvChapterError = view.findViewById(R.id.tvChapterError);

        ivCover = view.findViewById(R.id.ivStoryCover);
        progressCover = view.findViewById(R.id.progressCover);
        progressChapters = view.findViewById(R.id.progressChapters);

        btnBack = view.findViewById(R.id.btnBack);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        btnReadNow = view.findViewById(R.id.btnReadNow);

        rvChapters = view.findViewById(R.id.rvChapters);
    }

    private void setupChapterList() {
        rvChapters.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        chapterAdapter = new ChapterAdapter(this::openChapter);
        rvChapters.setAdapter(chapterAdapter);
    }

    private void setupListeners(View view) {
        btnBack.setOnClickListener(v -> {
            NavController navController =
                    Navigation.findNavController(view);

            navController.navigateUp();
        });

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        btnReadNow.setOnClickListener(v -> openReader(null));

        tvChapterError.setOnClickListener(v -> loadChapters());
    }

    private void loadStoryDetail() {
        executorService.execute(() -> {
            try {
                StoryEntity localStory =
                        storyRepository.getStoryById(storyId);

                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    if (localStory != null) {
                        bindStoryData(localStory);
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Không tìm thấy dữ liệu truyện",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Không thể tải thông tin truyện",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        });
    }

    private void bindStoryData(StoryEntity story) {
        tvTitle.setText(
                story.getTitle() != null
                        ? story.getTitle()
                        : "Chưa có tên"
        );

        tvAuthor.setText(
                "Tác giả: " +
                        (story.getAuthorId() != null
                                ? story.getAuthorId()
                                : "Đang cập nhật")
        );

        tvStatus.setText(
                story.getStatus() != null
                        ? story.getStatus()
                        : "Đang phát hành"
        );

        tvStats.setText(
                story.getChapterCount()
                        + " chương • "
                        + story.getFavoriteCount()
                        + " lượt thích"
        );

        tvDescription.setText(
                story.getDescription() != null
                        ? story.getDescription()
                        : "Chưa có mô tả cho truyện này."
        );

        loadCoverImage(story.getCoverUrl());
    }

    private void loadCoverImage(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            progressCover.setVisibility(View.GONE);
            ivCover.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
            return;
        }

        progressCover.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(coverUrl.trim())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        if (isAdded() && progressCover != null) {
                            progressCover.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        if (isAdded() && progressCover != null) {
                            progressCover.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
                .into(ivCover);
    }

    private void loadChapters() {
        progressChapters.setVisibility(View.VISIBLE);
        tvChapterEmpty.setVisibility(View.GONE);
        tvChapterError.setVisibility(View.GONE);
        rvChapters.setVisibility(View.GONE);

        chapterRepository.getChaptersFromCacheOrFirestore(
                storyId,
                new ChapterRepository.RepositoryCallback<List<ChapterEntity>>() {
                    @Override
                    public void onSuccess(List<ChapterEntity> result) {
                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressChapters.setVisibility(View.GONE);

                        List<ChapterEntity> chapters =
                                result == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(result);

                        chapters.sort(
                                Comparator.comparingInt(
                                        ChapterEntity::getChapterNumber
                                )
                        );

                        tvChapterCount.setText(
                                chapters.size() + " chương"
                        );

                        if (chapters.isEmpty()) {
                            rvChapters.setVisibility(View.GONE);
                            tvChapterEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        tvChapterEmpty.setVisibility(View.GONE);
                        rvChapters.setVisibility(View.VISIBLE);
                        chapterAdapter.setChapters(chapters);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        progressChapters.setVisibility(View.GONE);
                        rvChapters.setVisibility(View.GONE);
                        tvChapterError.setVisibility(View.VISIBLE);
                        tvChapterError.setText(
                                "Không thể tải danh sách chương. Nhấn để thử lại."
                        );
                    }
                }
        );
    }

    private void openChapter(ChapterEntity chapter) {
        if (chapter == null || chapter.getChapterId() == null) {
            return;
        }

        openReader(chapter.getChapterId());
    }

    private void openReader(@Nullable String chapterId) {
        if (storyId == null || storyId.trim().isEmpty()) {
            return;
        }

        Bundle args = new Bundle();
        args.putString(ARG_STORY_ID, storyId);

        if (chapterId != null && !chapterId.trim().isEmpty()) {
            args.putString(ARG_CHAPTER_ID, chapterId);
        }

        NavController navController =
                Navigation.findNavController(requireView());

        navController.navigate(
                R.id.action_storyDetailFragment_to_readerFragment,
                args
        );
    }

    private void checkFavoriteStatus() {
        String userId = getCurrentUserId();

        executorService.execute(() -> {
            try {
                boolean favorite = favoriteRepository.isFavorite(
                        userId,
                        storyId
                );

                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    isFav = favorite;
                    updateFavoriteIcon();
                });

            } catch (Exception ignored) {
                // Giữ trạng thái mặc định nếu không kiểm tra được.
            }
        });
    }

    private void toggleFavorite() {
        if (storyId == null || isFavoriteRequestRunning) {
            return;
        }

        isFavoriteRequestRunning = true;
        btnFavorite.setEnabled(false);

        String userId = getCurrentUserId();
        boolean shouldRemove = isFav;

        executorService.execute(() -> {
            try {
                if (shouldRemove) {
                    favoriteRepository.deleteFavorite(
                            userId,
                            storyId
                    );
                } else {
                    FavoriteEntity favorite = new FavoriteEntity(
                            UUID.randomUUID().toString(),
                            userId,
                            storyId,
                            System.currentTimeMillis()
                    );

                    favoriteRepository.insertFavorite(favorite);
                }

                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    isFav = !shouldRemove;
                    isFavoriteRequestRunning = false;
                    btnFavorite.setEnabled(true);

                    updateFavoriteIcon();

                    Toast.makeText(
                            requireContext(),
                            isFav
                                    ? "Đã lưu vào thư viện"
                                    : "Đã bỏ yêu thích",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    isFavoriteRequestRunning = false;
                    btnFavorite.setEnabled(true);

                    Toast.makeText(
                            requireContext(),
                            "Không thể cập nhật yêu thích",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        });
    }

    private void updateFavoriteIcon() {
        if (btnFavorite == null) {
            return;
        }

        btnFavorite.setImageResource(
                isFav
                        ? R.drawable.ic_favorite_filled
                        : R.drawable.ic_favorite_outline
        );

        btnFavorite.setContentDescription(
                isFav ? "Bỏ yêu thích" : "Thêm vào yêu thích"
        );
    }

    private String getCurrentUserId() {
        return UserSessionManager.getCurrentUserId(
                requireContext()
        );
    }

    @Override
    public void onDestroy() {
        if (ivCover != null) {
            Glide.with(this).clear(ivCover);
        }

        executorService.shutdown();
        mainHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }
}
