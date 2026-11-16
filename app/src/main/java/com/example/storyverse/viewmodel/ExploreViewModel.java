package com.example.storyverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.storyverse.data.local.entity.StoryEntity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ExploreViewModel extends AndroidViewModel {

    public static final int SORT_POPULAR = 0;
    public static final int SORT_NEWEST = 1;
    public static final int SORT_RATING = 2;

    private final MutableLiveData<List<StoryEntity>> featuredStories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<StoryEntity>> exploreStories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final List<StoryEntity> allStories = new ArrayList<>();
    private String selectedCategory = "Tất cả";
    private int currentSortType = SORT_POPULAR;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        loadExploreData();
    }

    public LiveData<List<StoryEntity>> getFeaturedStories() {
        return featuredStories;
    }

    public LiveData<List<StoryEntity>> getExploreStories() {
        return exploreStories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadExploreData() {
        isLoading.setValue(true);

        // TODO: Kết nối với StoryRepository hoặc MultiSourceStoryRepository để lấy dữ liệu thật
        // Ví dụ: storyRepository.getAllStories().observeForever(...)
        allStories.clear();
        allStories.addAll(createSampleStories());

        // Lấy tối đa 5 truyện đầu làm danh sách nổi bật
        int featuredLimit = Math.min(allStories.size(), 5);
        featuredStories.setValue(new ArrayList<>(allStories.subList(0, featuredLimit)));

        applyFilterAndSort();
        isLoading.setValue(false);
    }

    public void selectCategory(String category) {
        this.selectedCategory = (category != null && !category.isEmpty()) ? category : "Tất cả";
        applyFilterAndSort();
    }

    public void selectSortType(int sortType) {
        this.currentSortType = sortType;
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        List<StoryEntity> result = new ArrayList<>();
        String targetCategory = normalize(selectedCategory);

        for (StoryEntity story : allStories) {
            if (selectedCategory.equalsIgnoreCase("Tất cả")) {
                result.add(story);
            } else if (story.getGenre() != null && normalize(story.getGenre()).contains(targetCategory)) {
                result.add(story);
            }
        }

        // Sắp xếp danh sách theo tiêu chí đang chọn
        Collections.sort(result, (s1, s2) -> {
            if (currentSortType == SORT_RATING) {
                return Double.compare(s2.getRating(), s1.getRating());
            } else if (currentSortType == SORT_NEWEST) {
                return Long.compare(s2.getUpdatedAt(), s1.getUpdatedAt());
            } else {
                return Integer.compare(s2.getViews(), s1.getViews());
            }
        });

        exploreStories.setValue(result);
    }

    private String normalize(String input) {
        if (input == null) return "";
        String temp = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(temp)
                .replaceAll("")
                .replace('đ', 'd');
    }


}