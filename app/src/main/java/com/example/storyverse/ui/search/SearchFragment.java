
package com.example.storyverse.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.repository.MultiSourceStoryRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {

    private EditText edtSearch;
    private ImageView btnClearSearch;
    private ChipGroup chipGroupSearchCategory;
    private Chip chipSearchAll;
    private TextView tvSearchResultTitle;
    private RecyclerView rvSearchResults;
    private ProgressBar progressSearch;
    private TextView tvSearchMessage;

    private MultiSourceStoryRepository multiSourceRepository;
    private StoryAdapter storyAdapter;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Runnable searchRunnable;

    private final List<Story> searchResults = new ArrayList<>();
    private final List<Story> filteredResults = new ArrayList<>();

    private String currentQuery = "";
    private String selectedCategory = "Tất cả";

    private int searchRequestId = 0;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_search,
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
        initRepositories();
        setupRecyclerView();
        setupSearch();
        setupCategoryFilter();

        showMessage("Nhập tên truyện hoặc tác giả để tìm kiếm");
        updateResultTitle();
    }

    private void initViews(View view) {

        edtSearch = view.findViewById(R.id.edt_search);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        chipGroupSearchCategory =
                view.findViewById(R.id.chipGroupSearchCategory);
        chipSearchAll = view.findViewById(R.id.chipSearchAll);
        tvSearchResultTitle =
                view.findViewById(R.id.tvSearchResultTitle);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressSearch = view.findViewById(R.id.progress_search);
        tvSearchMessage = view.findViewById(R.id.tv_search_message);
    }

    private void initRepositories() {
        multiSourceRepository = new MultiSourceStoryRepository();
    }

    private void setupRecyclerView() {

        rvSearchResults.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        storyAdapter = new StoryAdapter(
                new StoryAdapter.OnStoryClickListener() {

                    @Override
                    public void onApiStoryClick(Story story) {
                        openStoryDetail(story);
                    }

                    @Override
                    public void onLocalStoryClick(StoryEntity story) {
                        // Sẽ tích hợp điều hướng truyện trong thư viện sau.
                    }
                }
        );

        rvSearchResults.setAdapter(storyAdapter);
    }

    private void setupSearch() {

        edtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String keyword = edtSearch.getText()
                        .toString()
                        .trim();

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }

                hideKeyboard();
                return true;
            }

            return false;
        });

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                currentQuery = s.toString().trim();

                if (btnClearSearch != null) {
                    btnClearSearch.setVisibility(
                            currentQuery.isEmpty()
                                    ? View.GONE
                                    : View.VISIBLE
                    );
                }

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (currentQuery.isEmpty()) {

                    searchRequestId++;

                    searchResults.clear();
                    filteredResults.clear();

                    storyAdapter.setApiStories(searchResults);

                    showLoading(false);
                    showMessage(
                            "Nhập tên truyện hoặc tác giả để tìm kiếm"
                    );

                    updateResultTitle();
                    return;
                }

                String keyword = currentQuery;

                searchRunnable = () -> performSearch(keyword);

                // Chờ người dùng ngừng nhập 600ms trước khi gọi API.
                handler.postDelayed(searchRunnable, 600);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                edtSearch.setText("");
                edtSearch.requestFocus();
            });
        }
    }

    private void setupCategoryFilter() {

        if (chipGroupSearchCategory == null) {
            return;
        }

        chipGroupSearchCategory.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {

                    if (checkedIds.isEmpty()) {

                        selectedCategory = "Tất cả";

                        if (chipSearchAll != null) {
                            chipSearchAll.setChecked(true);
                        }

                    } else {

                        Chip selectedChip =
                                group.findViewById(checkedIds.get(0));

                        if (selectedChip != null) {
                            selectedCategory =
                                    selectedChip.getText().toString();
                        }
                    }

                    filterSearchResults();
                }
        );
    }

    private void performSearch(String keyword) {

        if (multiSourceRepository == null) {
            showMessage("Không thể khởi tạo chức năng tìm kiếm");
            return;
        }

        final int currentRequestId = ++searchRequestId;

        showLoading(true);
        showMessage("Đang tìm kiếm truyện...");

        multiSourceRepository.searchStories(
                keyword,
                new MultiSourceStoryRepository.MultiSourceCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> stories,
                            List<String> warnings
                    ) {

                        if (!isAdded()
                                || currentRequestId != searchRequestId) {
                            return;
                        }

                        showLoading(false);

                        searchResults.clear();

                        if (stories != null) {
                            searchResults.addAll(stories);
                        }

                        if (warnings != null && !warnings.isEmpty()) {
                            android.util.Log.w(
                                    "SearchFragment",
                                    "Cảnh báo API: " + warnings
                            );
                        }

                        filterSearchResults();
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()
                                || currentRequestId != searchRequestId) {
                            return;
                        }

                        showLoading(false);

                        searchResults.clear();
                        filteredResults.clear();

                        storyAdapter.setApiStories(
                                new ArrayList<>()
                        );

                        showMessage(
                                message != null
                                        ? message
                                        : "Đã xảy ra lỗi khi tìm kiếm"
                        );

                        updateResultTitle();
                    }
                }
        );
    }

    private void filterSearchResults() {

        filteredResults.clear();

        String normalizedQuery =
                removeAccents(currentQuery.toLowerCase(Locale.ROOT));

        String normalizedCategory =
                removeAccents(selectedCategory.toLowerCase(Locale.ROOT));

        for (Story story : searchResults) {

            if (story == null) {
                continue;
            }

            boolean matchesCategory =
                    selectedCategory.equals("Tất cả");

            if (!matchesCategory) {

                String category = safeString(story.getCategory());

                String normalizedStoryCategory =
                        removeAccents(category.toLowerCase(Locale.ROOT));

                matchesCategory =
                        normalizedStoryCategory.contains(normalizedCategory);
            }

            boolean matchesQuery = true;

            if (!normalizedQuery.isEmpty()) {

                String title = removeAccents(
                        safeString(story.getTitle()).toLowerCase(Locale.ROOT)
                );

                String author = removeAccents(
                        safeString(story.getAuthor()).toLowerCase(Locale.ROOT)
                );

                matchesQuery =
                        title.contains(normalizedQuery)
                                || author.contains(normalizedQuery);
            }

            if (matchesCategory && matchesQuery) {
                filteredResults.add(story);
            }
        }

        storyAdapter.setApiStories(
                new ArrayList<>(filteredResults)
        );

        updateResultTitle();

        if (filteredResults.isEmpty()) {

            showMessage(
                    searchResults.isEmpty()
                            ? "Không tìm thấy truyện phù hợp"
                            : "Không có truyện thuộc thể loại này"
            );

        } else {

            tvSearchMessage.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }

        android.util.Log.d(
                "SearchFragment",
                "Hiển thị " + filteredResults.size() + " truyện"
        );
    }

    private void updateResultTitle() {

        if (tvSearchResultTitle == null) {
            return;
        }

        if (currentQuery.isEmpty()
                && selectedCategory.equals("Tất cả")) {

            tvSearchResultTitle.setText("Gợi ý tìm kiếm");

        } else {

            tvSearchResultTitle.setText(
                    String.format(
                            Locale.ROOT,
                            "Kết quả tìm kiếm (%d)",
                            filteredResults.size()
                    )
            );
        }
    }

    private void openStoryDetail(Story story) {

        if (story == null || !isAdded()) {
            return;
        }

        Bundle bundle = new Bundle();

        bundle.putString("storyId", story.getStoryId());
        bundle.putString("title", story.getTitle());
        bundle.putString("author", story.getAuthor());
        bundle.putString("description", story.getDescription());
        bundle.putString("coverUrl", story.getCoverUrl());
        bundle.putString("language", story.getLanguage());
        bundle.putString("source", story.getSource());
        bundle.putString("sourceUrl", story.getSourceUrl());

        Navigation.findNavController(requireView()).navigate(
                R.id.action_searchFragment_to_storyDetailFragment,
                bundle
        );
    }

    private void showLoading(boolean isLoading) {

        if (progressSearch != null) {
            progressSearch.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
        }
    }

    private void showMessage(String message) {

        if (tvSearchMessage != null) {

            tvSearchMessage.setText(message);
            tvSearchMessage.setVisibility(View.VISIBLE);
        }

        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {

        if (getContext() == null || edtSearch == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    edtSearch.getWindowToken(),
                    0
            );
        }
    }

    private String removeAccents(String input) {

        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(
                input,
                Normalizer.Form.NFD
        );

        Pattern pattern = Pattern.compile(
                "\\p{InCombiningDiacriticalMarks}+"
        );

        return pattern.matcher(normalized)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);
        searchRequestId++;

        super.onDestroyView();
    }
}
