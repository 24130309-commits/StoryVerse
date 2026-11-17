
package com.example.storyverse.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.local.entity.AuthorEntity;
import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.SearchHistoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.SearchHistoryRepository;
import com.example.storyverse.data.repository.StoryRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_CATEGORY";
    private static final String ARG_STORY_ID = "storyId";

    private RecyclerView rvFeaturedStories;
    private RecyclerView rvAllStories;

    private StoryAdapter featuredAdapter;
    private StoryAdapter verticalAdapter;

    private SearchHistoryRepository searchHistoryRepository;
    private StoryRepository storyRepository;

    private ChipGroup chipGroupGenre;
    private ChipGroup chipGroupRecentSearch;

    private LinearLayout layoutRecentSearch;

    private TextView tvSectionListTitle;

    private MaterialCardView cardSearchBar;

    private List<StoryEntity> allStoriesCache = new ArrayList<>();
    private List<CategoryEntity> allCategories = new ArrayList<>();

    private String selectedCategoryId = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_home,
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
        setupRecyclerViews();
        setupSearchHistoryDatabase();
        setupListeners();
        loadStoryData();
    }

    // =====================================================
    // KHỞI TẠO VIEW
    // =====================================================

    private void initViews(View view) {
        rvFeaturedStories = view.findViewById(R.id.rvFeaturedStories);
        rvAllStories = view.findViewById(R.id.rvAllStories);

        chipGroupGenre = view.findViewById(R.id.chipGroupGenre);
        chipGroupRecentSearch = view.findViewById(R.id.chipGroupRecentSearch);

        layoutRecentSearch = view.findViewById(R.id.layoutRecentSearch);
        tvSectionListTitle = view.findViewById(R.id.tvSectionListTitle);

        cardSearchBar = view.findViewById(R.id.cardSearchBar);
    }

    // =====================================================
    // RECYCLERVIEW
    // =====================================================

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
                this::onStorySelected
        );

        rvFeaturedStories.setAdapter(featuredAdapter);

        rvAllStories.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        verticalAdapter = new StoryAdapter(
                StoryAdapter.MODE_VERTICAL,
                this::onStorySelected
        );

        rvAllStories.setAdapter(verticalAdapter);

        rvAllStories.setNestedScrollingEnabled(false);
    }

    // =====================================================
    // LỊCH SỬ TÌM KIẾM
    // =====================================================

    private void setupSearchHistoryDatabase() {

        searchHistoryRepository = new SearchHistoryRepository(
                requireActivity().getApplication()
        );

        searchHistoryRepository.getRecentSearches().observe(
                getViewLifecycleOwner(),
                histories -> {

                    if (histories == null || histories.isEmpty()) {
                        layoutRecentSearch.setVisibility(View.GONE);
                        return;
                    }

                    layoutRecentSearch.setVisibility(View.VISIBLE);
                    chipGroupRecentSearch.removeAllViews();

                    for (SearchHistoryEntity item : histories) {

                        Chip chip = new Chip(requireContext());

                        chip.setText(item.getKeyword());
                        chip.setCheckable(false);

                        chip.setOnClickListener(v ->
                                openSearchFragmentWithKeyword(
                                        item.getKeyword()
                                )
                        );

                        chipGroupRecentSearch.addView(chip);
                    }
                }
        );
    }

    // =====================================================
    // TẢI DỮ LIỆU HOME
    // =====================================================

    private void loadStoryData() {

        storyRepository = new StoryRepository(requireContext());

        storyRepository.syncPublishedStoriesFromFirestore(
                new StoryRepository.RepositoryCallback<Integer>() {

                    @Override
                    public void onSuccess(Integer count) {

                        if (!isAdded()) {
                            return;
                        }

                        Log.d(TAG,
                                "Đồng bộ Firestore thành công. Số truyện: " + count);

                        loadHomeDataFromRoom();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(TAG,
                                "Không thể đồng bộ Firestore, chuyển sang Room", e);

                        if (!isAdded()) {
                            return;
                        }

                        loadHomeDataFromRoom();
                    }
                }
        );
    }

    private void loadHomeDataFromRoom() {

        storyRepository.getPublishedStories(
                new StoryRepository.RepositoryCallback<List<StoryEntity>>() {

                    @Override
                    public void onSuccess(List<StoryEntity> stories) {

                        if (!isAdded()) {
                            return;
                        }

                        allStoriesCache = stories != null
                                ? new ArrayList<>(stories)
                                : new ArrayList<>();

                        Log.d(TAG,
                                "Số truyện đã xuất bản trong Room: "
                                        + allStoriesCache.size());

                        displayAllStories();

                        loadStoryMetadata();
                        loadCategories();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(TAG,
                                "Không thể tải danh sách truyện từ Room", e);

                        if (!isAdded()) {
                            return;
                        }

                        allStoriesCache.clear();

                        featuredAdapter.setStories(allStoriesCache);
                        verticalAdapter.setStories(allStoriesCache);

                        Toast.makeText(
                                requireContext(),
                                "Không thể tải danh sách truyện",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadCategories();
                    }
                }
        );
    }

    // =====================================================
    // HIỂN THỊ TRUYỆN
    // =====================================================

    private void displayAllStories() {

        featuredAdapter.setStories(allStoriesCache);
        verticalAdapter.setStories(allStoriesCache);

        tvSectionListTitle.setText("Đề Xuất Cho Bạn");
    }

    // =====================================================
    // TẢI TÊN TÁC GIẢ VÀ THỂ LOẠI
    // =====================================================

    private void loadStoryMetadata() {

        if (allStoriesCache.isEmpty()) {
            return;
        }

        storyRepository.getAuthors(
                new StoryRepository.RepositoryCallback<List<AuthorEntity>>() {

                    @Override
                    public void onSuccess(List<AuthorEntity> authors) {

                        if (!isAdded()) {
                            return;
                        }

                        Map<String, String> authorMap = new HashMap<>();

                        if (authors != null) {
                            for (AuthorEntity author : authors) {

                                if (author == null
                                        || author.getAuthorId() == null) {
                                    continue;
                                }

                                authorMap.put(
                                        author.getAuthorId(),
                                        author.getName()
                                );
                            }
                        }

                        loadStoryGenres(authorMap);
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(TAG,
                                "Không thể tải thông tin tác giả", e);

                        if (!isAdded()) {
                            return;
                        }

                        loadStoryGenres(new HashMap<>());
                    }
                }
        );
    }

    private void loadStoryGenres(Map<String, String> authorMap) {

        if (allStoriesCache.isEmpty()) {
            return;
        }

        Map<String, String> genreMap = new LinkedHashMap<>();

        final int totalStories = allStoriesCache.size();
        final int[] completedCount = {0};

        for (StoryEntity story : allStoriesCache) {

            if (story == null
                    || story.getStoryId() == null
                    || story.getStoryId().trim().isEmpty()) {

                completedCount[0]++;
                continue;
            }

            storyRepository.getCategoriesByStoryId(
                    story.getStoryId(),
                    new StoryRepository.RepositoryCallback<List<CategoryEntity>>() {

                        @Override
                        public void onSuccess(List<CategoryEntity> categories) {

                            if (!isAdded()) {
                                return;
                            }

                            if (categories != null && !categories.isEmpty()) {

                                List<String> names = new ArrayList<>();

                                for (CategoryEntity category : categories) {

                                    if (category != null
                                            && category.getName() != null
                                            && !category.getName().trim().isEmpty()) {

                                        names.add(category.getName());
                                    }
                                }

                                if (!names.isEmpty()) {
                                    genreMap.put(
                                            story.getStoryId(),
                                            android.text.TextUtils.join(", ", names)
                                    );
                                }
                            }

                            completedCount[0]++;

                            if (completedCount[0] >= totalStories) {
                                updateAdapterMetadata(authorMap, genreMap);
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                            Log.e(TAG,
                                    "Không thể tải thể loại của truyện: "
                                            + story.getStoryId(), e);

                            if (!isAdded()) {
                                return;
                            }

                            completedCount[0]++;

                            if (completedCount[0] >= totalStories) {
                                updateAdapterMetadata(authorMap, genreMap);
                            }
                        }
                    }
            );
        }

        if (completedCount[0] >= totalStories) {
            updateAdapterMetadata(authorMap, genreMap);
        }
    }

    private void updateAdapterMetadata(
            Map<String, String> authorMap,
            Map<String, String> genreMap
    ) {

        if (!isAdded()) {
            return;
        }

        featuredAdapter.setLocalMetadata(authorMap, genreMap);
        verticalAdapter.setLocalMetadata(authorMap, genreMap);
    }

    // =====================================================
    // TẢI DANH MỤC THỂ LOẠI
    // =====================================================

    private void loadCategories() {

        Log.d(TAG, "Bắt đầu tải danh mục từ Room");

        storyRepository.getCategories(
                new StoryRepository.RepositoryCallback<List<CategoryEntity>>() {

                    @Override
                    public void onSuccess(List<CategoryEntity> categories) {

                        if (!isAdded()) {
                            return;
                        }

                        allCategories = categories != null
                                ? new ArrayList<>(categories)
                                : new ArrayList<>();

                        Log.d(TAG,
                                "Tải danh mục thành công. Số lượng: "
                                        + allCategories.size());

                        for (CategoryEntity category : allCategories) {
                            if (category != null) {
                                Log.d(TAG,
                                        "Category: "
                                                + category.getCategoryId()
                                                + " | "
                                                + category.getName());
                            }
                        }

                        setupGenreChips();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(TAG,
                                "Lỗi khi tải danh mục thể loại từ Room", e);

                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                "Không thể tải danh mục thể loại",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setupGenreChips() {

        chipGroupGenre.removeAllViews();

        chipGroupGenre.setSingleSelection(true);
        chipGroupGenre.setSelectionRequired(true);

        Chip chipAll = createGenreChip("Tất cả", "");

        chipGroupGenre.addView(chipAll);

        for (CategoryEntity category : allCategories) {

            if (category == null
                    || category.getCategoryId() == null
                    || category.getName() == null) {
                continue;
            }

            Chip chip = createGenreChip(
                    category.getName(),
                    category.getCategoryId()
            );

            chipGroupGenre.addView(chip);
        }

        chipAll.setChecked(true);
    }

    private Chip createGenreChip(
            String name,
            String categoryId
    ) {

        Chip chip = new Chip(requireContext());

        chip.setId(View.generateViewId());
        chip.setText(name);
        chip.setTag(categoryId);

        chip.setCheckable(true);
        chip.setClickable(true);

        return chip;
    }

    // =====================================================
    // LỌC TRUYỆN THEO THỂ LOẠI
    // =====================================================

    private void filterStoriesByCategory(
            String categoryId,
            String categoryName
    ) {

        selectedCategoryId = categoryId == null ? "" : categoryId.trim();

        if (selectedCategoryId.isEmpty()) {

            tvSectionListTitle.setText("Đề Xuất Cho Bạn");

            verticalAdapter.setStories(new ArrayList<>(allStoriesCache));

            return;
        }

        final String requestedCategoryId = selectedCategoryId;

        tvSectionListTitle.setText("Thể loại: " + categoryName);

        storyRepository.getPublishedStoriesByCategory(
                requestedCategoryId,
                50,
                0,
                new StoryRepository.RepositoryCallback<List<StoryEntity>>() {

                    @Override
                    public void onSuccess(List<StoryEntity> stories) {

                        if (!isAdded()
                                || !requestedCategoryId.equals(selectedCategoryId)) {
                            return;
                        }

                        verticalAdapter.setStories(
                                stories != null
                                        ? stories
                                        : new ArrayList<>()
                        );
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(TAG,
                                "Không thể lọc truyện theo thể loại: "
                                        + requestedCategoryId, e);

                        if (!isAdded()
                                || !requestedCategoryId.equals(selectedCategoryId)) {
                            return;
                        }

                        verticalAdapter.setStories(new ArrayList<>());

                        Toast.makeText(
                                requireContext(),
                                "Không thể lọc truyện theo thể loại",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // SỰ KIỆN
    // =====================================================

    private void setupListeners() {

        cardSearchBar.setOnClickListener(v ->
                openSearchFragmentWithKeyword("")
        );

        chipGroupGenre.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds == null || checkedIds.isEmpty()) {
                        return;
                    }

                    Chip selectedChip = group.findViewById(
                            checkedIds.get(0)
                    );

                    if (selectedChip == null) {
                        return;
                    }

                    Object tag = selectedChip.getTag();

                    String categoryId = tag == null
                            ? ""
                            : tag.toString();

                    filterStoriesByCategory(
                            categoryId,
                            selectedChip.getText().toString()
                    );
                }
        );
    }

    private void openSearchFragmentWithKeyword(String keyword) {
        if (!isAdded() || getView() == null) {
            return;
        }

        Bundle args = new Bundle();
        args.putString("SEARCH_KEYWORD", keyword == null ? "" : keyword);

        try {
            NavController navController =
                    NavHostFragment.findNavController(this);

            navController.navigate(
                    R.id.action_homeFragment2_to_searchFragment,
                    args
            );

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Không thể mở màn hình tìm kiếm", e);

            Toast.makeText(
                    requireContext(),
                    "Không thể mở màn hình tìm kiếm",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    // =====================================================
    // ĐIỀU HƯỚNG CHI TIẾT TRUYỆN
    // =====================================================

    private void onStorySelected(StoryEntity story) {

        if (!isAdded()) {
            return;
        }

        if (story == null
                || story.getStoryId() == null
                || story.getStoryId().trim().isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Không tìm thấy thông tin truyện",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String storyId = story.getStoryId().trim();

        Bundle bundle = new Bundle();
        bundle.putString(ARG_STORY_ID, storyId);

        try {
            NavController navController =
                    NavHostFragment.findNavController(this);

            navController.navigate(
                    R.id.action_homeFragment2_to_storyDetailFragment,
                    bundle
            );

        } catch (IllegalArgumentException e) {

            Log.e(TAG,
                    "Không thể điều hướng đến StoryDetailFragment", e);

            Toast.makeText(
                    requireContext(),
                    "Không thể mở chi tiết truyện",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
