
package com.example.storyverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.storyverse.data.local.entity.CategoryEntity;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.repository.StoryRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreViewModel extends AndroidViewModel {

    public static final int SORT_POPULAR = 0;
    public static final int SORT_NEWEST = 1;
    public static final int SORT_RATING = 2;

    private static final int PAGE_SIZE = 20;
    private static final int FEATURED_LIMIT = 10;
    private static final String ALL_CATEGORIES = "Tất cả";

    private final StoryRepository storyRepository;

    private final MutableLiveData<Boolean> isLoading =
            new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> isLoadingMore =
            new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> hasMore =
            new MutableLiveData<>(true);

    private final MutableLiveData<List<CategoryEntity>> categories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<StoryEntity>> featuredStories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<StoryEntity>> exploreStories =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<String> error =
            new MutableLiveData<>();

    private final List<StoryEntity> loadedStories =
            new ArrayList<>();

    private final Set<String> loadedStoryIds =
            new HashSet<>();

    private String selectedCategory = ALL_CATEGORIES;

    private int selectedSortType = SORT_POPULAR;
    private int currentOffset = 0;
    private int requestId = 0;

    private boolean categoriesLoaded = false;
    private boolean loadingPage = false;

    public ExploreViewModel(@NonNull Application application) {
        super(application);

        storyRepository = new StoryRepository(application);

        loadInitialData();
    }

    // LiveData

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getIsLoadingMore() {
        return isLoadingMore;
    }

    public LiveData<Boolean> getHasMore() {
        return hasMore;
    }

    public LiveData<List<CategoryEntity>> getCategories() {
        return categories;
    }

    public LiveData<List<StoryEntity>> getFeaturedStories() {
        return featuredStories;
    }

    public LiveData<List<StoryEntity>> getExploreStories() {
        return exploreStories;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Khởi tạo dữ liệu

    private void loadInitialData() {
        isLoading.setValue(true);

        loadCategories();
        loadFeaturedStories();
    }

    private void loadCategories() {
        storyRepository.getCategories(
                new StoryRepository.RepositoryCallback<List<CategoryEntity>>() {
                    @Override
                    public void onSuccess(List<CategoryEntity> result) {
                        List<CategoryEntity> safeList =
                                result == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(result);

                        categories.setValue(safeList);
                        categoriesLoaded = true;

                        loadFirstPage();
                    }

                    @Override
                    public void onError(Exception e) {
                        categoriesLoaded = true;
                        categories.setValue(new ArrayList<>());

                        loadFirstPage();
                    }
                }
        );
    }

    // Truyện nổi bật

    private void loadFeaturedStories() {
        storyRepository.getPublishedStoriesPaged(
                50,
                0,
                new StoryRepository.RepositoryCallback<List<StoryEntity>>() {
                    @Override
                    public void onSuccess(List<StoryEntity> result) {
                        List<StoryEntity> stories =
                                result == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(result);

                        sortByPopular(stories);

                        int end = Math.min(
                                FEATURED_LIMIT,
                                stories.size()
                        );

                        featuredStories.setValue(
                                new ArrayList<>(stories.subList(0, end))
                        );
                    }

                    @Override
                    public void onError(Exception e) {
                        featuredStories.setValue(new ArrayList<>());
                    }
                }
        );
    }

    // Lọc thể loại

    public void selectCategory(String categoryName) {
        String newCategory =
                categoryName == null || categoryName.trim().isEmpty()
                        ? ALL_CATEGORIES
                        : categoryName.trim();

        if (selectedCategory.equalsIgnoreCase(newCategory)) {
            return;
        }

        selectedCategory = newCategory;

        if (categoriesLoaded) {
            loadFirstPage();
        }
    }

    // Sắp xếp

    public void selectSortType(int sortType) {
        if (sortType != SORT_POPULAR
                && sortType != SORT_NEWEST
                && sortType != SORT_RATING) {
            return;
        }

        selectedSortType = sortType;
        updateDisplayedStories();
    }

    // Tải trang đầu tiên

    public void refresh() {
        if (!categoriesLoaded) {
            return;
        }

        loadFirstPage();
    }

    private void loadFirstPage() {
        requestId++;

        loadedStories.clear();
        loadedStoryIds.clear();

        currentOffset = 0;
        loadingPage = false;

        isLoading.setValue(true);
        isLoadingMore.setValue(false);
        hasMore.setValue(true);
        error.setValue(null);

        loadPage(requestId, false);
    }

    // Phân trang

    public void loadNextPage() {
        if (!categoriesLoaded
                || loadingPage
                || Boolean.TRUE.equals(isLoading.getValue())
                || !Boolean.TRUE.equals(hasMore.getValue())) {
            return;
        }

        loadPage(requestId, true);
    }

    private void loadPage(int currentRequestId, boolean isNextPage) {
        if (loadingPage) {
            return;
        }

        loadingPage = true;

        if (isNextPage) {
            isLoadingMore.setValue(true);
        }

        int offset = currentOffset;

        if (ALL_CATEGORIES.equalsIgnoreCase(selectedCategory)) {
            storyRepository.getPublishedStoriesPaged(
                    PAGE_SIZE,
                    offset,
                    createPageCallback(currentRequestId, isNextPage)
            );

            return;
        }

        String categoryId = findCategoryId(selectedCategory);

        if (categoryId == null) {
            loadingPage = false;
            isLoading.setValue(false);
            isLoadingMore.setValue(false);
            hasMore.setValue(false);
            updateDisplayedStories();
            return;
        }

        storyRepository.getPublishedStoriesByCategory(
                categoryId,
                PAGE_SIZE,
                offset,
                createPageCallback(currentRequestId, isNextPage)
        );
    }

    private StoryRepository.RepositoryCallback<List<StoryEntity>>
    createPageCallback(int currentRequestId, boolean isNextPage) {

        return new StoryRepository.RepositoryCallback<List<StoryEntity>>() {

            @Override
            public void onSuccess(List<StoryEntity> result) {
                if (currentRequestId != requestId) {
                    return;
                }

                loadingPage = false;

                List<StoryEntity> page =
                        result == null ? new ArrayList<>() : result;

                for (StoryEntity story : page) {
                    if (story == null || story.getStoryId() == null) {
                        continue;
                    }

                    if (loadedStoryIds.add(story.getStoryId())) {
                        loadedStories.add(story);
                    }
                }

                currentOffset += page.size();

                hasMore.setValue(page.size() == PAGE_SIZE);

                updateDisplayedStories();

                isLoading.setValue(false);
                isLoadingMore.setValue(false);
                error.setValue(null);
            }

            @Override
            public void onError(Exception e) {
                if (currentRequestId != requestId) {
                    return;
                }

                loadingPage = false;

                isLoading.setValue(false);
                isLoadingMore.setValue(false);

                error.setValue(getErrorMessage(
                        e,
                        isNextPage
                                ? "Không thể tải thêm truyện"
                                : "Không thể tải danh sách truyện"
                ));
            }
        };
    }

    // Cập nhật danh sách hiển thị

    private void updateDisplayedStories() {
        List<StoryEntity> result =
                new ArrayList<>(loadedStories);

        switch (selectedSortType) {
            case SORT_NEWEST:
                sortByNewest(result);
                break;

            case SORT_RATING:
                sortByRating(result);
                break;

            case SORT_POPULAR:
            default:
                sortByPopular(result);
                break;
        }

        exploreStories.setValue(result);
    }

    // Các tiêu chí sắp xếp

    private void sortByPopular(List<StoryEntity> stories) {
        Collections.sort(stories,
                Comparator.comparingLong(StoryEntity::getViewCount)
                        .reversed()
        );
    }

    private void sortByNewest(List<StoryEntity> stories) {
        Collections.sort(stories,
                Comparator.comparingLong(StoryEntity::getCreatedAt)
                        .reversed()
        );
    }

    private void sortByRating(List<StoryEntity> stories) {
        Collections.sort(stories,
                Comparator.comparingDouble(StoryEntity::getRatingAverage)
                        .reversed()
        );
    }

    // Tìm mã danh mục từ tên danh mục

    private String findCategoryId(String categoryName) {
        List<CategoryEntity> categoryList = categories.getValue();

        if (categoryList == null) {
            return null;
        }

        for (CategoryEntity category : categoryList) {
            if (category == null
                    || category.getName() == null
                    || category.getCategoryId() == null) {
                continue;
            }

            if (category.getName().trim()
                    .equalsIgnoreCase(categoryName)) {
                return category.getCategoryId();
            }
        }

        return null;
    }

    // Xử lý thông báo lỗi

    private String getErrorMessage(
            Exception exception,
            String defaultMessage
    ) {
        if (exception != null
                && exception.getMessage() != null
                && !exception.getMessage().trim().isEmpty()) {
            return exception.getMessage();
        }

        return defaultMessage;
    }
}
