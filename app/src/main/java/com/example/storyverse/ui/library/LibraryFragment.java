
package com.example.storyverse.ui.library;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.LibraryStoryAdapter;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.utils.UserSessionManager;
import com.example.storyverse.viewmodel.LibraryViewModel;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private static final int TAB_FAVORITES = 0;
    private static final int TAB_READING = 1;
    private static final int TAB_HISTORY = 2;

    private RecyclerView rvLibrary;

    private TextView tvLibraryCount;
    private TextView tvEmptyLibraryTitle;
    private TextView tvEmptyLibrary;
    private TextView tvLibraryError;

    private LinearLayout layoutLibraryEmpty;
    private LinearLayout layoutLibraryError;

    private ProgressBar progressLibrary;

    private Button btnTabFavorites;
    private Button btnTabReading;
    private Button btnTabHistory;
    private Button btnRetryLibrary;

    private LibraryStoryAdapter adapter;
    private LibraryViewModel viewModel;

    private String currentUserId;
    private int currentTab = TAB_FAVORITES;

    public LibraryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_library,
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

        bindViews(view);
        setupRecyclerView();
        setupViewModel();
        setupTabButtons();
        setupRetryButton();

        currentUserId = UserSessionManager.getCurrentUserId(
                requireContext()
        );

        observeViewModel();

        selectTab(TAB_FAVORITES, false);
    }

    private void bindViews(View view) {
        rvLibrary = view.findViewById(R.id.rvLibrary);

        tvLibraryCount = view.findViewById(R.id.tvLibraryCount);
        tvEmptyLibraryTitle = view.findViewById(R.id.tvEmptyLibraryTitle);
        tvEmptyLibrary = view.findViewById(R.id.tvEmptyLibrary);
        tvLibraryError = view.findViewById(R.id.tvLibraryError);

        layoutLibraryEmpty = view.findViewById(R.id.layoutLibraryEmpty);
        layoutLibraryError = view.findViewById(R.id.layoutLibraryError);

        progressLibrary = view.findViewById(R.id.progressLibrary);

        btnTabFavorites = view.findViewById(R.id.btnTabFavorites);
        btnTabReading = view.findViewById(R.id.btnTabReading);
        btnTabHistory = view.findViewById(R.id.btnTabHistory);
        btnRetryLibrary = view.findViewById(R.id.btnRetryLibrary);
    }

    private void setupRecyclerView() {
        rvLibrary.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter = new LibraryStoryAdapter(
                new LibraryStoryAdapter.OnLibraryItemListener() {
                    @Override
                    public void onStoryClick(StoryEntity story) {
                        openStoryDetail(story);
                    }

                    @Override
                    public void onRemoveClick(StoryEntity story) {
                        if (story == null || story.getStoryId() == null) {
                            return;
                        }

                        viewModel.removeFavorite(
                                currentUserId,
                                story.getStoryId()
                        );
                    }
                }
        );

        rvLibrary.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this)
                .get(LibraryViewModel.class);
    }

    private void setupTabButtons() {
        btnTabFavorites.setOnClickListener(v ->
                selectTab(TAB_FAVORITES, true)
        );

        btnTabReading.setOnClickListener(v ->
                selectTab(TAB_READING, true)
        );

        btnTabHistory.setOnClickListener(v ->
                selectTab(TAB_HISTORY, true)
        );
    }

    private void setupRetryButton() {
        btnRetryLibrary.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.retryLastLoad();
            }
        });
    }

    private void observeViewModel() {

        viewModel.getFavoriteStories().observe(
                getViewLifecycleOwner(),
                stories -> {
                    if (currentTab == TAB_FAVORITES) {
                        renderCurrentTab();
                    }
                }
        );

        viewModel.getReadingStories().observe(
                getViewLifecycleOwner(),
                stories -> {
                    if (currentTab == TAB_READING) {
                        renderCurrentTab();
                    }
                }
        );

        viewModel.getHistoryStories().observe(
                getViewLifecycleOwner(),
                stories -> {
                    if (currentTab == TAB_HISTORY) {
                        renderCurrentTab();
                    }
                }
        );

        viewModel.getReadingProgressMap().observe(
                getViewLifecycleOwner(),
                progressMap -> {
                    adapter.setReadingProgressMap(progressMap);
                    renderCurrentTab();
                }
        );

        viewModel.getHistoryReadAtMap().observe(
                getViewLifecycleOwner(),
                readAtMap -> {
                    adapter.setHistoryReadAtMap(readAtMap);
                    renderCurrentTab();
                }
        );

        viewModel.getLoading().observe(
                getViewLifecycleOwner(),
                isLoading -> {
                    boolean loading = Boolean.TRUE.equals(isLoading);

                    progressLibrary.setVisibility(
                            loading ? View.VISIBLE : View.GONE
                    );

                    renderCurrentTab();
                }
        );

        viewModel.getErrorMessage().observe(
                getViewLifecycleOwner(),
                error -> {
                    boolean hasError = error != null
                            && !error.trim().isEmpty();

                    layoutLibraryError.setVisibility(
                            hasError ? View.VISIBLE : View.GONE
                    );

                    if (hasError) {
                        tvLibraryError.setText(error);
                    }

                    renderCurrentTab();
                }
        );
    }

    private void selectTab(int tab, boolean loadData) {
        currentTab = tab;

        updateTabAppearance();

        if (loadData && currentUserId != null) {
            loadSelectedTab();
        }

        renderCurrentTab();
    }

    private void updateTabAppearance() {
        updateButtonAppearance(
                btnTabFavorites,
                currentTab == TAB_FAVORITES
        );

        updateButtonAppearance(
                btnTabReading,
                currentTab == TAB_READING
        );

        updateButtonAppearance(
                btnTabHistory,
                currentTab == TAB_HISTORY
        );
    }

    private void updateButtonAppearance(
            Button button,
            boolean selected
    ) {
        int backgroundColor = selected
                ? 0xFF5B4BDB
                : 0xFFF0EEFA;

        int textColor = selected
                ? 0xFFFFFFFF
                : 0xFF625D70;

        button.setBackgroundTintList(
                ColorStateList.valueOf(backgroundColor)
        );

        button.setTextColor(textColor);
    }

    private void loadSelectedTab() {
        switch (currentTab) {
            case TAB_FAVORITES:
                viewModel.loadFavorites(currentUserId);
                break;

            case TAB_READING:
                viewModel.loadReadingStories(currentUserId);
                break;

            case TAB_HISTORY:
                viewModel.loadHistoryStories(currentUserId);
                break;
        }
    }

    private void renderCurrentTab() {
        if (adapter == null || viewModel == null) {
            return;
        }

        List<StoryEntity> stories = getCurrentStories();

        if (stories == null) {
            stories = new ArrayList<>();
        }

        adapter.setDisplayMode(currentTab);

        adapter.setReadingProgressMap(
                viewModel.getReadingProgressMap().getValue()
        );

        adapter.setHistoryReadAtMap(
                viewModel.getHistoryReadAtMap().getValue()
        );

        adapter.setStories(stories);

        tvLibraryCount.setText(
                stories.size() + " truyện"
        );

        updateEmptyContent(stories);
        updateErrorContent(stories);
    }

    private List<StoryEntity> getCurrentStories() {
        switch (currentTab) {
            case TAB_READING:
                return viewModel.getReadingStories().getValue();

            case TAB_HISTORY:
                return viewModel.getHistoryStories().getValue();

            case TAB_FAVORITES:
            default:
                return viewModel.getFavoriteStories().getValue();
        }
    }

    private void updateEmptyContent(List<StoryEntity> stories) {
        boolean isEmpty = stories.isEmpty();

        Boolean loadingValue = viewModel.getLoading().getValue();
        boolean isLoading = Boolean.TRUE.equals(loadingValue);

        String error = viewModel.getErrorMessage().getValue();
        boolean hasError = error != null && !error.trim().isEmpty();

        boolean showEmpty = isEmpty && !isLoading && !hasError;

        layoutLibraryEmpty.setVisibility(
                showEmpty ? View.VISIBLE : View.GONE
        );

        if (!showEmpty) {
            return;
        }

        switch (currentTab) {
            case TAB_FAVORITES:
                tvEmptyLibraryTitle.setText(
                        "Chưa có truyện yêu thích"
                );
                tvEmptyLibrary.setText(
                        "Hãy lưu những câu chuyện bạn yêu thích để đọc lại sau."
                );
                break;

            case TAB_READING:
                tvEmptyLibraryTitle.setText(
                        "Chưa có truyện đang đọc"
                );
                tvEmptyLibrary.setText(
                        "Những truyện bạn bắt đầu đọc sẽ xuất hiện ở đây."
                );
                break;

            case TAB_HISTORY:
                tvEmptyLibraryTitle.setText(
                        "Lịch sử đọc đang trống"
                );
                tvEmptyLibrary.setText(
                        "Những truyện bạn đã đọc gần đây sẽ được lưu tại đây."
                );
                break;
        }
    }

    private void updateErrorContent(List<StoryEntity> stories) {
        String error = viewModel.getErrorMessage().getValue();

        boolean hasError = error != null && !error.trim().isEmpty();

        layoutLibraryError.setVisibility(
                hasError ? View.VISIBLE : View.GONE
        );

        if (hasError) {
            tvLibraryError.setText(error);
        }

        if (hasError && stories.isEmpty()) {
            layoutLibraryEmpty.setVisibility(View.GONE);
        }
    }

    private void openStoryDetail(StoryEntity story) {
        if (story == null
                || story.getStoryId() == null
                || story.getStoryId().trim().isEmpty()) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("storyId", story.getStoryId());

        NavHostFragment.findNavController(this).navigate(
                R.id.action_libraryFragment_to_storyDetailFragment,
                bundle
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null && currentUserId != null) {
            viewModel.loadLibrary(currentUserId);
        }
    }
}
