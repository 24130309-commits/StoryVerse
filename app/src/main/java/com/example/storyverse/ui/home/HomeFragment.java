package com.example.storyverse.ui.home; // Sửa lại đúng package của file HomeFragment hiện tại

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.local.entity.SearchHistoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.SearchHistoryRepository;
import com.example.storyverse.adapter.StoryAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvFeaturedStories, rvAllStories;
    private StoryAdapter featuredAdapter, verticalAdapter;
    private SearchHistoryRepository searchHistoryRepository;

    private ChipGroup chipGroupGenre, chipGroupRecentSearch;
    private LinearLayout layoutRecentSearch;
    private TextView tvSectionListTitle;
    private MaterialCardView cardSearchBar;

    private List<StoryEntity> allStoriesCache = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupSearchHistoryDatabase();
        loadStoryData();
        setupListeners();
    }

    private void initViews(View view) {
        rvFeaturedStories = view.findViewById(R.id.rvFeaturedStories);
        rvAllStories = view.findViewById(R.id.rvAllStories);
        chipGroupGenre = view.findViewById(R.id.chipGroupGenre);
        chipGroupRecentSearch = view.findViewById(R.id.chipGroupRecentSearch);
        layoutRecentSearch = view.findViewById(R.id.layoutRecentSearch);
        tvSectionListTitle = view.findViewById(R.id.tvSectionListTitle);
        cardSearchBar = view.findViewById(R.id.cardSearchBar);
    }

    private void setupRecyclerViews() {
        rvFeaturedStories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredAdapter = new StoryAdapter(StoryAdapter.MODE_HORIZONTAL, this::onStorySelected);
        rvFeaturedStories.setAdapter(featuredAdapter);

        rvAllStories.setLayoutManager(new LinearLayoutManager(requireContext()));
        verticalAdapter = new StoryAdapter(StoryAdapter.MODE_VERTICAL, this::onStorySelected);
        rvAllStories.setAdapter(verticalAdapter);
    }

    private void setupSearchHistoryDatabase() {
        searchHistoryRepository = new SearchHistoryRepository(requireActivity().getApplication());
        searchHistoryRepository.getRecentSearches().observe(getViewLifecycleOwner(), histories -> {
            if (histories == null || histories.isEmpty()) {
                layoutRecentSearch.setVisibility(View.GONE);
                return;
            }
            layoutRecentSearch.setVisibility(View.VISIBLE);
            chipGroupRecentSearch.removeAllViews();

            for (SearchHistoryEntity item : histories) {
                Chip chip = new Chip(requireContext());
                chip.setText(item.getKeyword());
                chip.setOnClickListener(v -> openSearchFragmentWithKeyword(item.getKeyword()));
                chipGroupRecentSearch.addView(chip);
            }
        });
    }

    private void loadStoryData() {
        // Dữ liệu mẫu tạm thời dùng StoryEntity để chạy độc lập Phần 2
        // Khi Thành viên 3 hoàn thành StoryRepository, chỉ cần đổ dữ liệu từ DB vào allStoriesCache
        allStoriesCache = createMockStoryEntities();

        featuredAdapter.setStories(allStoriesCache);
        verticalAdapter.setStories(allStoriesCache);
    }

    private void filterStoriesByGenre(String genre) {
        if (genre.equals("Tất cả")) {
            tvSectionListTitle.setText("Đề Xuất Cho Bạn");
            verticalAdapter.setStories(allStoriesCache);
            return;
        }

        tvSectionListTitle.setText("Thể loại: " + genre);
        // TODO: Khi mở StoryEntity.java thấy tên hàm lấy thể loại (vd: getGenre() hoặc getCategory()),
        // hãy mở comment đoạn lọc dưới đây:
        /*
        List<StoryEntity> filtered = new ArrayList<>();
        for (StoryEntity s : allStoriesCache) {
            if (s.getGenre() != null && s.getGenre().equalsIgnoreCase(genre)) {
                filtered.add(s);
            }
        }
        verticalAdapter.setStories(filtered);
        */
        verticalAdapter.setStories(allStoriesCache);
    }

    private void setupListeners() {
        cardSearchBar.setOnClickListener(v -> openSearchFragmentWithKeyword(""));

        chipGroupGenre.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip selectedChip = group.findViewById(checkedIds.get(0));
            if (selectedChip != null) {
                filterStoriesByGenre(selectedChip.getText().toString());
            }
        });
    }

    private void openSearchFragmentWithKeyword(String keyword) {
        Bundle args = new Bundle();
        args.putString("SEARCH_KEYWORD", keyword);

        // TODO: Bỏ comment khi bạn hoàn thành SearchFragment (thuộc Phần 2)
        /*
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(args);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
        */
        Toast.makeText(requireContext(), "Mở SearchFragment: " + keyword, Toast.LENGTH_SHORT).show();
    }

    private void onStorySelected(StoryEntity story) {
        // Thuộc Phần 3 (StoryDetailFragment) - Chỉ gắn khi ghép code với Thành viên 3
        Toast.makeText(requireContext(), "Đã chọn: " + story.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private List<StoryEntity> createMockStoryEntities() {
        List<StoryEntity> list = new ArrayList<>();
        String[] titles = {
                "Đấu Phá Thương Khung",
                "Thám Tử Lừng Danh",
                "Vũ Trụ Song Song",
                "Gió Ấm Không Bằng Anh Thâm Tình"
        };

        for (String title : titles) {
            StoryEntity entity = new StoryEntity();
            entity.setTitle(title);
            // Nếu StoryEntity có thêm setAuthor, setGenre, setCoverUrl... bạn có thể gán thêm ở đây
            list.add(entity);
        }
        return list;
    }
}