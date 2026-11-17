
package com.example.storyverse.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.viewmodel.ExploreViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ExploreFragment extends Fragment {

    private MaterialCardView cardExploreSearch;

    private TextView tvSeeAllFeatured;
    private TextView tvExploreSectionTitle;
    private TextView tvExploreSort;
    private TextView tvEmptyExplore;

    private RecyclerView rvFeaturedStories;
    private RecyclerView rvExploreStories;

    private ChipGroup chipGroupExploreCategory;
    private Chip chipExploreAll;

    private ProgressBar progressBarExplore;

    private NestedScrollView nestedScrollExplore;

    private StoryAdapter featuredAdapter;
    private StoryAdapter exploreAdapter;

    private ExploreViewModel viewModel;

    private boolean isShowingError = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_explore,
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

        viewModel = new ViewModelProvider(this)
                .get(ExploreViewModel.class);

        setupRecyclerViews();
        setupListeners();
        setupPagination();
        observeViewModel();
    }

    private void initViews(View view) {

        nestedScrollExplore =
                (NestedScrollView) view;

        cardExploreSearch =
                view.findViewById(R.id.cardExploreSearch);

        tvSeeAllFeatured =
                view.findViewById(R.id.tvSeeAllFeatured);

        tvExploreSectionTitle =
                view.findViewById(R.id.tvExploreSectionTitle);

        tvExploreSort =
                view.findViewById(R.id.tvExploreSort);

        tvEmptyExplore =
                view.findViewById(R.id.tvEmptyExplore);

        rvFeaturedStories =
                view.findViewById(R.id.rvFeaturedStories);

        rvExploreStories =
                view.findViewById(R.id.rvExploreStories);

        chipGroupExploreCategory =
                view.findViewById(R.id.chipGroupExploreCategory);

        chipExploreAll =
                view.findViewById(R.id.chipExploreAll);

        progressBarExplore =
                view.findViewById(R.id.progressBarExplore);
    }

    private void setupRecyclerViews() {

        rvFeaturedStories.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        featuredAdapter = new StoryAdapter(
                StoryAdapter.MODE_HORIZONTAL,
                this::openStoryDetail
        );

        rvFeaturedStories.setAdapter(featuredAdapter);

        rvExploreStories.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        exploreAdapter = new StoryAdapter(
                StoryAdapter.MODE_VERTICAL,
                this::openStoryDetail
        );

        rvExploreStories.setAdapter(exploreAdapter);

        rvFeaturedStories.setNestedScrollingEnabled(false);
        rvExploreStories.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {

        cardExploreSearch.setOnClickListener(
                v -> navigateToSearch()
        );

        tvSeeAllFeatured.setOnClickListener(v -> {

            chipExploreAll.setChecked(true);

            tvExploreSectionTitle.setText(
                    "Danh Sách Truyện"
            );

            tvExploreSort.setText("Phổ biến");

            viewModel.selectCategory("Tất cả");

            viewModel.selectSortType(
                    ExploreViewModel.SORT_POPULAR
            );
        });

        chipGroupExploreCategory.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {
                        return;
                    }

                    Chip selectedChip =
                            group.findViewById(checkedIds.get(0));

                    if (selectedChip == null) {
                        return;
                    }

                    String category =
                            selectedChip.getText().toString();

                    if ("Tất cả".equalsIgnoreCase(category)) {
                        tvExploreSectionTitle.setText(
                                "Danh Sách Truyện"
                        );
                    } else {
                        tvExploreSectionTitle.setText(
                                "Thể loại: " + category
                        );
                    }

                    viewModel.selectCategory(category);
                }
        );

        tvExploreSort.setOnClickListener(
                this::showSortMenu
        );
    }

    private void showSortMenu(View anchor) {

        PopupMenu popupMenu = new PopupMenu(
                requireContext(),
                anchor
        );

        popupMenu.getMenu().add(
                0,
                ExploreViewModel.SORT_POPULAR,
                0,
                "Phổ biến"
        );

        popupMenu.getMenu().add(
                0,
                ExploreViewModel.SORT_NEWEST,
                1,
                "Mới nhất"
        );

        popupMenu.getMenu().add(
                0,
                ExploreViewModel.SORT_RATING,
                2,
                "Đánh giá cao"
        );

        popupMenu.setOnMenuItemClickListener(item -> {

            tvExploreSort.setText(item.getTitle());

            viewModel.selectSortType(item.getItemId());

            return true;
        });

        popupMenu.show();
    }

    private void setupPagination() {

        nestedScrollExplore.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener)
                        (scrollView, scrollX, scrollY,
                         oldScrollX, oldScrollY) -> {

                            View content =
                                    scrollView.getChildAt(0);

                            if (content == null) {
                                return;
                            }

                            int distanceToBottom =
                                    content.getMeasuredHeight()
                                            - scrollView.getHeight()
                                            - scrollY;

                            if (distanceToBottom <= 300) {
                                viewModel.loadNextPage();
                            }
                        }
        );
    }

    private void observeViewModel() {

        viewModel.getIsLoading().observe(
                getViewLifecycleOwner(),
                loading -> {

                    boolean isLoading =
                            Boolean.TRUE.equals(loading);

                    progressBarExplore.setVisibility(
                            isLoading ? View.VISIBLE : View.GONE
                    );
                }
        );

        viewModel.getFeaturedStories().observe(
                getViewLifecycleOwner(),
                stories -> featuredAdapter.setStories(stories)
        );

        viewModel.getExploreStories().observe(
                getViewLifecycleOwner(),
                stories -> {

                    exploreAdapter.setStories(stories);

                    boolean isEmpty =
                            stories == null || stories.isEmpty();

                    rvExploreStories.setVisibility(
                            isEmpty ? View.GONE : View.VISIBLE
                    );

                    tvEmptyExplore.setVisibility(
                            isEmpty ? View.VISIBLE : View.GONE
                    );
                }
        );

        viewModel.getError().observe(
                getViewLifecycleOwner(),
                message -> {

                    if (message == null || message.trim().isEmpty()) {
                        isShowingError = false;
                        return;
                    }

                    if (!isShowingError && isAdded()) {

                        isShowingError = true;

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void navigateToSearch() {

        NavController navController =
                Navigation.findNavController(requireView());

        navController.navigate(
                R.id.action_exploreFragment_to_searchFragment
        );
    }

    private void openStoryDetail(StoryEntity story) {

        if (story == null
                || story.getStoryId() == null
                || story.getStoryId().trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Không tìm thấy mã truyện",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Bundle args = new Bundle();

        args.putString(
                "storyId",
                story.getStoryId()
        );

        NavController navController =
                Navigation.findNavController(requireView());

        navController.navigate(
                R.id.action_exploreFragment_to_storyDetailFragment,
                args
        );
    }
}
