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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.ui.search.SearchFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ExploreFragment extends Fragment {

    private MaterialCardView cardExploreSearch;
    private TextView tvSeeAllFeatured;
    private RecyclerView rvFeaturedStories;
    private ChipGroup chipGroupExploreCategory;
    private Chip chipExploreAll;
    private TextView tvExploreSectionTitle;
    private TextView tvExploreSort;
    private ProgressBar progressBarExplore;
    private RecyclerView rvExploreStories;
    private TextView tvEmptyExplore;

    private StoryAdapter featuredAdapter;
    private StoryAdapter exploreAdapter;
    private ExploreViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews(@NonNull View view) {
        cardExploreSearch = view.findViewById(R.id.cardExploreSearch);
        tvSeeAllFeatured = view.findViewById(R.id.tvSeeAllFeatured);
        rvFeaturedStories = view.findViewById(R.id.rvFeaturedStories);
        chipGroupExploreCategory = view.findViewById(R.id.chipGroupExploreCategory);
        chipExploreAll = view.findViewById(R.id.chipExploreAll);
        tvExploreSectionTitle = view.findViewById(R.id.tvExploreSectionTitle);
        tvExploreSort = view.findViewById(R.id.tvExploreSort);
        progressBarExplore = view.findViewById(R.id.progressBarExplore);
        rvExploreStories = view.findViewById(R.id.rvExploreStories);
        tvEmptyExplore = view.findViewById(R.id.tvEmptyExplore);
    }

    private void setupRecyclerViews() {
        // 1. Danh sách truyện nổi bật (cuộn ngang)
        rvFeaturedStories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        featuredAdapter = new StoryAdapter(StoryAdapter.MODE_HORIZONTAL, this::openStoryDetail);
        rvFeaturedStories.setAdapter(featuredAdapter);

        // 2. Danh sách truyện khám phá theo bộ lọc (cuộn dọc)
        rvExploreStories.setLayoutManager(new LinearLayoutManager(requireContext()));
        exploreAdapter = new StoryAdapter(StoryAdapter.MODE_VERTICAL, this::openStoryDetail);
        rvExploreStories.setAdapter(exploreAdapter);
    }

    private void setupListeners() {
        // Mở màn hình SearchFragment khi nhấn vào thanh tìm kiếm
        cardExploreSearch.setOnClickListener(v -> navigateToSearch());

        // Xem tất cả truyện nổi bật -> chuyển về bộ lọc "Tất cả" và sắp xếp Phổ biến
        tvSeeAllFeatured.setOnClickListener(v -> {
            chipExploreAll.setChecked(true);
            tvExploreSort.setText("Phổ biến");
            viewModel.selectCategory("Tất cả");
            viewModel.selectSortType(ExploreViewModel.SORT_POPULAR);
        });

        // Lọc truyện khi chọn Chip thể loại
        chipGroupExploreCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                chipExploreAll.setChecked(true);
                tvExploreSectionTitle.setText("Danh Sách Truyện");
                viewModel.selectCategory("Tất cả");
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String category = selectedChip.getText().toString();
                    tvExploreSectionTitle.setText(
                            category.equals("Tất cả") ? "Danh Sách Truyện" : "Thể loại: " + category
                    );
                    viewModel.selectCategory(category);
                }
            }
        });

        // Menu chọn kiểu sắp xếp
        tvExploreSort.setOnClickListener(this::showSortMenu);
    }

    private void showSortMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenu().add(0, ExploreViewModel.SORT_POPULAR, 0, "Phổ biến");
        popupMenu.getMenu().add(0, ExploreViewModel.SORT_NEWEST, 1, "Mới nhất");
        popupMenu.getMenu().add(0, ExploreViewModel.SORT_RATING, 2, "Đánh giá cao");

        popupMenu.setOnMenuItemClickListener(item -> {
            tvExploreSort.setText(item.getTitle());
            viewModel.selectSortType(item.getItemId());
            return true;
        });
        popupMenu.show();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                progressBarExplore.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );

        viewModel.getFeaturedStories().observe(getViewLifecycleOwner(), stories ->
                featuredAdapter.setStories(stories)
        );

        viewModel.getExploreStories().observe(getViewLifecycleOwner(), stories -> {
            exploreAdapter.setStories(stories);
            boolean isEmpty = (stories == null || stories.isEmpty());
            rvExploreStories.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvEmptyExplore.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    private void navigateToSearch() {
        // Thay R.id.nav_host_fragment bằng ID FrameLayout / FragmentContainerView trong MainActivity của bạn
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(((ViewGroup) requireView().getParent()).getId(), new SearchFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openStoryDetail(StoryEntity story) {
        // TODO: Điều hướng sang StoryDetailFragment hoặc StoryDetailActivity kèm story.getId()
        Toast.makeText(requireContext(), "Mở truyện: " + story.getTitle(), Toast.LENGTH_SHORT).show();
    }
}