
package com.example.storyverse.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_CATEGORY";
    private static final String ARG_STORY_ID = "storyId";

    private static final int MAX_FEATURED_STORIES = 5;
    private static final int CATEGORY_PAGE_SIZE = 50;

    private RecyclerView rvFeaturedStories;
    private RecyclerView rvAllStories;

    private StoryAdapter featuredAdapter;
    private StoryAdapter verticalAdapter;

    private SearchHistoryRepository searchHistoryRepository;
    private StoryRepository storyRepository;

    private ChipGroup chipGroupGenre;
    private ChipGroup chipGroupRecentSearch;

    private LinearLayout layoutRecentSearch;
    private LinearLayout layoutHomeEmpty;
    private LinearLayout layoutHomeError;

    private ProgressBar progressHome;

    private TextView tvSectionListTitle;

    private MaterialCardView cardSearchBar;

    private List<StoryEntity> allStoriesCache = new ArrayList<>();
    private List<StoryEntity> recommendationStoriesCache = new ArrayList<>();
    private List<CategoryEntity> allCategories = new ArrayList<>();

    private final Set<String> featuredStoryIds = new HashSet<>();

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

        showLoading();
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
        layoutHomeEmpty = view.findViewById(R.id.layoutHomeEmpty);
        layoutHomeError = view.findViewById(R.id.layoutHomeError);

        progressHome = view.findViewById(R.id.progressHome);

        tvSectionListTitle = view.findViewById(R.id.tvSectionListTitle);

        cardSearchBar = view.findViewById(R.id.cardSearchBar);
    }

    // =====================================================
    // TRẠNG THÁI GIAO DIỆN
    // =====================================================

    private void showLoading() {

        progressHome.setVisibility(View.VISIBLE);

        layoutHomeEmpty.setVisibility(View.GONE);
        layoutHomeError.setVisibility(View.GONE);

        rvFeaturedStories.setVisibility(View.GONE);
        rvAllStories.setVisibility(View.GONE);
    }

    private void showListLoading() {

        progressHome.setVisibility(View.VISIBLE);

        layoutHomeEmpty.setVisibility(View.GONE);
        layoutHomeError.setVisibility(View.GONE);

        rvFeaturedStories.setVisibility(
                allStoriesCache.isEmpty() ? View.GONE : View.VISIBLE
        );

        rvAllStories.setVisibility(View.GONE);
    }

    private void showContentState(boolean isEmpty) {

        progressHome.setVisibility(View.GONE);
        layoutHomeError.setVisibility(View.GONE);

        rvFeaturedStories.setVisibility(
                allStoriesCache.isEmpty() ? View.GONE : View.VISIBLE
        );

        if (isEmpty) {

            rvAllStories.setVisibility(View.GONE);
            layoutHomeEmpty.setVisibility(View.VISIBLE);

        } else {

            rvAllStories.setVisibility(View.VISIBLE);
            layoutHomeEmpty.setVisibility(View.GONE);
        }
    }

    private void showError() {

        progressHome.setVisibility(View.GONE);

        rvFeaturedStories.setVisibility(View.GONE);
        rvAllStories.setVisibility(View.GONE);

        layoutHomeEmpty.setVisibility(View.GONE);
        layoutHomeError.setVisibility(View.VISIBLE);
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
        rvFeaturedStories.setNestedScrollingEnabled(false);
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

                    if (!isAdded() || getView() == null) {
                        return;
                    }

                    if (histories == null || histories.isEmpty()) {

                        layoutRecentSearch.setVisibility(View.GONE);
                        return;
                    }

                    layoutRecentSearch.setVisibility(View.VISIBLE);
                    chipGroupRecentSearch.removeAllViews();

                    for (SearchHistoryEntity item : histories) {

                        if (item == null
                                || item.getKeyword() == null
                                || item.getKeyword().trim().isEmpty()) {
                            continue;
                        }

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

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        Log.d(
                                TAG,
                                "Đồng bộ Firestore thành công. Số truyện: " + count
                        );

                        loadHomeDataFromRoom();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(
                                TAG,
                                "Không thể đồng bộ Firestore, chuyển sang Room",
                                e
                        );

                        if (!isAdded() || getView() == null) {
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

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        allStoriesCache = stories != null
                                ? new ArrayList<>(stories)
                                : new ArrayList<>();

                        Log.d(
                                TAG,
                                "Số truyện đã xuất bản trong Room: "
                                        + allStoriesCache.size()
                        );

                        prepareHomeStories();

                        loadStoryMetadata();
                        loadCategories();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(
                                TAG,
                                "Không thể tải danh sách truyện từ Room",
                                e
                        );

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        allStoriesCache.clear();
                        recommendationStoriesCache.clear();
                        featuredStoryIds.clear();

                        featuredAdapter.setStories(new ArrayList<>());
                        verticalAdapter.setStories(new ArrayList<>());

                        showError();

                        loadCategories();
                    }
                }
        );
    }

    // =====================================================
    // PHÂN LOẠI TRUYỆN NỔI BẬT VÀ ĐỀ XUẤT
    // =====================================================

    private void prepareHomeStories() {

        List<StoryEntity> sortedStories = new ArrayList<>();

        for (StoryEntity story : allStoriesCache) {

            if (story == null || story.getStoryId() == null) {
                continue;
            }

            sortedStories.add(story);
        }

        // Xếp hạng truyện theo mức độ quan tâm.
        Collections.sort(sortedStories, new Comparator<StoryEntity>() {
            @Override
            public int compare(StoryEntity first, StoryEntity second) {

                int result = Long.compare(
                        second.getViewCount(),
                        first.getViewCount()
                );

                if (result != 0) {
                    return result;
                }

                result = Double.compare(
                        second.getRatingAverage(),
                        first.getRatingAverage()
                );

                if (result != 0) {
                    return result;
                }

                result = Long.compare(
                        second.getFavoriteCount(),
                        first.getFavoriteCount()
                );

                if (result != 0) {
                    return result;
                }

                return Long.compare(
                        second.getFavoriteCount(),
                        first.getFavoriteCount()
                );
            }
        });

        // Chỉ lấy một phần danh sách làm truyện nổi bật.
        int featuredCount = getFeaturedStoryCount(sortedStories.size());

        List<StoryEntity> featuredStories = new ArrayList<>();

        featuredStoryIds.clear();

        for (int i = 0; i < featuredCount; i++) {

            StoryEntity story = sortedStories.get(i);

            featuredStories.add(story);
            featuredStoryIds.add(story.getStoryId());
        }

        // Các truyện còn lại được xếp theo thời gian cập nhật.
        recommendationStoriesCache = new ArrayList<>();

        for (StoryEntity story : sortedStories) {

            if (!featuredStoryIds.contains(story.getStoryId())) {
                recommendationStoriesCache.add(story);
            }
        }

        Collections.sort(
                recommendationStoriesCache,
                (first, second) -> Long.compare(
                        getStorySortTime(second),
                        getStorySortTime(first)
                )
        );

        featuredAdapter.setStories(featuredStories);

        if (selectedCategoryId.isEmpty()) {

            verticalAdapter.setStories(
                    new ArrayList<>(recommendationStoriesCache)
            );

            tvSectionListTitle.setText("Đề xuất cho bạn");
        }

        showContentState(recommendationStoriesCache.isEmpty());
    }

    private int getFeaturedStoryCount(int totalStories) {

        if (totalStories < 2) {
            return 0;
        }

        return Math.min(
                MAX_FEATURED_STORIES,
                Math.max(1, totalStories / 4)
        );
    }

    private long getStorySortTime(StoryEntity story) {

        if (story.getUpdatedAt() > 0) {
            return story.getUpdatedAt();
        }

        return story.getCreatedAt();
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

                        if (!isAdded() || getView() == null) {
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

                        Log.e(
                                TAG,
                                "Không thể tải thông tin tác giả",
                                e
                        );

                        if (!isAdded() || getView() == null) {
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

                            if (!isAdded() || getView() == null) {
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

                            Log.e(
                                    TAG,
                                    "Không thể tải thể loại của truyện: "
                                            + story.getStoryId(),
                                    e
                            );

                            if (!isAdded() || getView() == null) {
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

        if (!isAdded() || getView() == null) {
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

                        if (!isAdded() || getView() == null) {
                            return;
                        }

                        allCategories = categories != null
                                ? new ArrayList<>(categories)
                                : new ArrayList<>();

                        Log.d(
                                TAG,
                                "Tải danh mục thành công. Số lượng: "
                                        + allCategories.size()
                        );

                        setupGenreChips();
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(
                                TAG,
                                "Lỗi khi tải danh mục thể loại từ Room",
                                e
                        );

                        if (!isAdded() || getView() == null) {
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

        selectedCategoryId = categoryId == null
                ? ""
                : categoryId.trim();

        if (selectedCategoryId.isEmpty()) {

            tvSectionListTitle.setText("Đề xuất cho bạn");

            verticalAdapter.setStories(
                    new ArrayList<>(recommendationStoriesCache)
            );

            showContentState(recommendationStoriesCache.isEmpty());

            return;
        }

        final String requestedCategoryId = selectedCategoryId;

        tvSectionListTitle.setText("Thể loại: " + categoryName);

        // Chỉ tải lại danh sách đề xuất, giữ nguyên truyện nổi bật.
        showListLoading();

        storyRepository.getPublishedStoriesByCategory(
                requestedCategoryId,
                CATEGORY_PAGE_SIZE,
                0,
                new StoryRepository.RepositoryCallback<List<StoryEntity>>() {

                    @Override
                    public void onSuccess(List<StoryEntity> stories) {

                        if (!isAdded()
                                || getView() == null
                                || !requestedCategoryId.equals(selectedCategoryId)) {
                            return;
                        }

                        List<StoryEntity> filteredStories = new ArrayList<>();

                        if (stories != null) {

                            for (StoryEntity story : stories) {

                                if (story == null
                                        || story.getStoryId() == null) {
                                    continue;
                                }

                                // Không lặp truyện đã nằm trong khu vực nổi bật.
                                if (!featuredStoryIds.contains(story.getStoryId())) {
                                    filteredStories.add(story);
                                }
                            }
                        }

                        Collections.sort(
                                filteredStories,
                                (first, second) -> Long.compare(
                                        getStorySortTime(second),
                                        getStorySortTime(first)
                                )
                        );

                        verticalAdapter.setStories(filteredStories);

                        showContentState(filteredStories.isEmpty());
                    }

                    @Override
                    public void onError(Exception e) {

                        Log.e(
                                TAG,
                                "Không thể lọc truyện theo thể loại: "
                                        + requestedCategoryId,
                                e
                        );

                        if (!isAdded()
                                || getView() == null
                                || !requestedCategoryId.equals(selectedCategoryId)) {
                            return;
                        }

                        verticalAdapter.setStories(new ArrayList<>());

                        showError();
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

        args.putString(
                "SEARCH_KEYWORD",
                keyword == null ? "" : keyword
        );

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

        if (!isAdded() || getView() == null) {
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

            Log.e(
                    TAG,
                    "Không thể điều hướng đến StoryDetailFragment",
                    e
            );

            Toast.makeText(
                    requireContext(),
                    "Không thể mở chi tiết truyện",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
