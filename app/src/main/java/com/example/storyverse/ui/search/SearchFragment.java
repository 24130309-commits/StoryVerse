
package com.example.storyverse.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.json.JSONArray;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {

    private static final String PREF_NAME = "storyverse_search_preferences";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String ARG_SEARCH_KEYWORD = "SEARCH_KEYWORD";

    private static final int MAX_HISTORY = 8;
    private static final int MAX_SUGGESTIONS = 7;
    private static final long SEARCH_DEBOUNCE = 600L;

    private EditText edtSearch;
    private ImageView btnClearSearch;
    private ImageButton btnBackSearch;

    private ChipGroup chipGroupSearchCategory;
    private Chip chipSearchAll;

    private TextView tvSearchResultTitle;
    private TextView tvSearchMessage;
    private TextView tvSearchWarning;
    private TextView tvSearchSuggestionTitle;
    private TextView btnClearSearchHistory;

    private ProgressBar progressSearch;
    private Button btnRetrySearch;

    private RecyclerView rvSearchResults;

    private LinearLayout layoutSearchSuggestions;
    private LinearLayout containerSearchSuggestions;

    private MultiSourceStoryRepository multiSourceRepository;
    private StoryAdapter storyAdapter;

    private SharedPreferences preferences;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final List<Story> searchResults = new ArrayList<>();
    private final List<Story> filteredResults = new ArrayList<>();

    private String currentQuery = "";
    private String lastSubmittedQuery = "";
    private String selectedCategory = "Tất cả";

    private int searchRequestId = 0;

    public SearchFragment() {
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

        setupBackButton();
        setupSearch();
        setupCategoryFilter();
        setupRetryButton();
        setupSearchHistory();

        showMessage(
                "Nhập tên truyện hoặc tác giả để tìm kiếm",
                false
        );

        updateResultTitle();
        updateSuggestions("");

        handleIncomingKeyword();
    }

    private void initViews(View view) {

        btnBackSearch = view.findViewById(R.id.btnBackSearch);

        edtSearch = view.findViewById(R.id.edt_search);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        chipGroupSearchCategory =
                view.findViewById(R.id.chipGroupSearchCategory);

        chipSearchAll =
                view.findViewById(R.id.chipSearchAll);

        tvSearchResultTitle =
                view.findViewById(R.id.tvSearchResultTitle);

        tvSearchMessage =
                view.findViewById(R.id.tv_search_message);

        tvSearchWarning =
                view.findViewById(R.id.tvSearchWarning);

        progressSearch =
                view.findViewById(R.id.progress_search);

        btnRetrySearch =
                view.findViewById(R.id.btnRetrySearch);

        rvSearchResults =
                view.findViewById(R.id.rv_search_results);

        layoutSearchSuggestions =
                view.findViewById(R.id.layoutSearchSuggestions);

        containerSearchSuggestions =
                view.findViewById(R.id.containerSearchSuggestions);

        tvSearchSuggestionTitle =
                view.findViewById(R.id.tvSearchSuggestionTitle);

        btnClearSearchHistory =
                view.findViewById(R.id.btnClearSearchHistory);
    }

    private void initRepositories() {

        multiSourceRepository = new MultiSourceStoryRepository();

        preferences = requireContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
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
                        // Search hiện tại chỉ hiển thị kết quả từ API.
                    }
                }
        );

        rvSearchResults.setAdapter(storyAdapter);
    }

    private void setupBackButton() {

        if (btnBackSearch == null) {
            return;
        }

        btnBackSearch.setOnClickListener(v -> {

            hideKeyboard();

            if (!isAdded() || getView() == null) {
                return;
            }

            boolean popped = Navigation
                    .findNavController(requireView())
                    .popBackStack();

            if (!popped) {
                Navigation
                        .findNavController(requireView())
                        .navigateUp();
            }
        });
    }

    private void handleIncomingKeyword() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        String keyword = args.getString(ARG_SEARCH_KEYWORD, "");

        if (keyword == null || keyword.trim().isEmpty()) {

            edtSearch.post(() -> {

                if (!isAdded() || edtSearch == null) {
                    return;
                }

                edtSearch.requestFocus();

                InputMethodManager imm =
                        (InputMethodManager) requireContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(
                            edtSearch,
                            InputMethodManager.SHOW_IMPLICIT
                    );
                }
            });

            return;
        }

        keyword = keyword.trim();

        edtSearch.setText(keyword);
        edtSearch.setSelection(edtSearch.getText().length());

        cancelPendingSearch();
        performSearch(keyword);
    }

    private void setupSearch() {

        edtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {

            if (hasFocus) {
                updateSuggestions(currentQuery);
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String keyword = edtSearch.getText()
                        .toString()
                        .trim();

                cancelPendingSearch();

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

                btnClearSearch.setVisibility(
                        currentQuery.isEmpty()
                                ? View.GONE
                                : View.VISIBLE
                );

                cancelPendingSearch();

                if (currentQuery.isEmpty()) {

                    searchRequestId++;

                    searchResults.clear();
                    filteredResults.clear();

                    storyAdapter.setApiStories(searchResults);

                    showLoading(false);
                    hideWarning();

                    showMessage(
                            "Nhập tên truyện hoặc tác giả để tìm kiếm",
                            false
                    );

                    updateResultTitle();
                    updateSuggestions("");

                    return;
                }

                updateSuggestions(currentQuery);

                String keyword = currentQuery;

                searchRunnable = () -> {
                    if (isAdded()) {
                        performSearch(keyword);
                    }
                };

                handler.postDelayed(
                        searchRunnable,
                        SEARCH_DEBOUNCE
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnClearSearch.setOnClickListener(v -> {

            edtSearch.setText("");
            edtSearch.requestFocus();

            if (getContext() != null) {

                InputMethodManager imm =
                        (InputMethodManager) requireContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(
                            edtSearch,
                            InputMethodManager.SHOW_IMPLICIT
                    );
                }
            }
        });
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

                    if (!searchResults.isEmpty()) {
                        filterSearchResults();
                    } else {
                        updateSuggestions(currentQuery);
                        updateResultTitle();
                    }
                }
        );
    }

    private void setupRetryButton() {

        btnRetrySearch.setOnClickListener(v -> {

            if (!lastSubmittedQuery.isEmpty()) {
                performSearch(lastSubmittedQuery);
            }
        });
    }

    private void setupSearchHistory() {

        btnClearSearchHistory.setOnClickListener(v -> {

            if (preferences != null) {
                preferences.edit()
                        .remove(KEY_SEARCH_HISTORY)
                        .apply();
            }

            updateSuggestions(currentQuery);
        });
    }

    private void performSearch(String keyword) {

        if (multiSourceRepository == null || !isAdded()) {
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String query = keyword.trim();

        cancelPendingSearch();

        currentQuery = query;
        lastSubmittedQuery = query;

        final int currentRequestId = ++searchRequestId;

        saveSearchHistory(query);

        layoutSearchSuggestions.setVisibility(View.GONE);

        showLoading(true);
        hideWarning();

        showMessage("Đang tìm kiếm truyện...", false);

        multiSourceRepository.searchStories(
                query,
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
                            showWarning(warnings);
                        } else {
                            hideWarning();
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

                        storyAdapter.setApiStories(new ArrayList<>());

                        hideWarning();
                        updateResultTitle();

                        if (isSourceError(message)) {

                            showMessage(
                                    "Không thể tải dữ liệu từ nguồn truyện. "
                                            + "Vui lòng kiểm tra kết nối Internet.",
                                    true
                            );

                        } else {

                            showMessage(
                                    "Không tìm thấy truyện phù hợp với từ khóa \""
                                            + query + "\".",
                                    false
                            );
                        }
                    }
                }
        );
    }

    private void filterSearchResults() {

        filteredResults.clear();

        String normalizedQuery =
                normalizeText(currentQuery);

        String normalizedCategory =
                normalizeText(selectedCategory);

        for (Story story : searchResults) {

            if (story == null) {
                continue;
            }

            boolean matchesCategory =
                    selectedCategory.equals("Tất cả")
                            || storyMatchesCategory(
                            story,
                            normalizedCategory
                    );

            boolean matchesQuery = true;

            if (!normalizedQuery.isEmpty()) {

                String title = normalizeText(story.getTitle());
                String author = normalizeText(story.getAuthor());

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
                            ? "Không tìm thấy truyện phù hợp."
                            : "Không có truyện thuộc thể loại này.",
                    false
            );

        } else {

            tvSearchMessage.setVisibility(View.GONE);
            btnRetrySearch.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    private boolean storyMatchesCategory(
            Story story,
            String normalizedCategory
    ) {

        String category = normalizeText(story.getCategory());

        if (category.contains(normalizedCategory)) {
            return true;
        }

        List<String> categories = story.getCategories();

        if (categories != null) {

            for (String item : categories) {

                if (normalizeText(item).contains(normalizedCategory)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateSuggestions(String query) {

        if (containerSearchSuggestions == null
                || layoutSearchSuggestions == null
                || preferences == null) {
            return;
        }

        containerSearchSuggestions.removeAllViews();

        String normalizedQuery = normalizeText(query);

        Set<String> suggestions = new LinkedHashSet<>();

        List<String> history = getSearchHistory();

        if (normalizedQuery.isEmpty()) {

            suggestions.addAll(history);

            for (String genre :
                    MultiSourceStoryRepository.getDefaultGenreKeywords()) {

                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    break;
                }

                suggestions.add(genre);
            }

        } else {

            for (String item : history) {

                if (normalizeText(item).contains(normalizedQuery)) {
                    suggestions.add(item);
                }
            }

            for (String genre :
                    MultiSourceStoryRepository.getDefaultGenreKeywords()) {

                if (normalizeText(genre).contains(normalizedQuery)) {
                    suggestions.add(genre);
                }

                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    break;
                }
            }
        }

        List<String> visibleSuggestions =
                new ArrayList<>(suggestions);

        if (visibleSuggestions.size() > MAX_SUGGESTIONS) {
            visibleSuggestions = visibleSuggestions.subList(
                    0,
                    MAX_SUGGESTIONS
            );
        }

        boolean hasHistory = !history.isEmpty();

        btnClearSearchHistory.setVisibility(
                hasHistory ? View.VISIBLE : View.GONE
        );

        if (normalizedQuery.isEmpty()) {

            tvSearchSuggestionTitle.setText(
                    hasHistory
                            ? "Tìm kiếm gần đây"
                            : "Từ khóa phổ biến"
            );

        } else {

            tvSearchSuggestionTitle.setText("Gợi ý từ khóa");
            btnClearSearchHistory.setVisibility(View.GONE);
        }

        if (visibleSuggestions.isEmpty()) {

            if (!normalizedQuery.isEmpty()) {

                TextView emptySuggestion = new TextView(requireContext());

                emptySuggestion.setText(
                        "Chưa có gợi ý phù hợp. Nhấn tìm kiếm để tra cứu."
                );

                emptySuggestion.setTextColor(0xFF8A849F);
                emptySuggestion.setTextSize(13);
                emptySuggestion.setPadding(4, 12, 4, 12);

                containerSearchSuggestions.addView(emptySuggestion);
            }

        } else {

            for (String suggestion : visibleSuggestions) {
                addSuggestionRow(
                        suggestion,
                        history.contains(suggestion)
                );
            }
        }

        layoutSearchSuggestions.setVisibility(
                visibleSuggestions.isEmpty() && normalizedQuery.isEmpty()
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    private void addSuggestionRow(
            String suggestion,
            boolean isHistory
    ) {

        TextView item = new TextView(requireContext());

        item.setText(suggestion);
        item.setTextSize(14);
        item.setTextColor(0xFF34304D);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(
                8,
                dpToPx(11),
                8,
                dpToPx(11)
        );

        item.setCompoundDrawablesWithIntrinsicBounds(
                isHistory
                        ? android.R.drawable.ic_menu_recent_history
                        : android.R.drawable.ic_menu_search,
                0,
                0,
                0
        );

        item.setCompoundDrawablePadding(dpToPx(12));

        item.setBackgroundResource(
                android.R.drawable.list_selector_background
        );

        item.setOnClickListener(v -> {

            edtSearch.setText(suggestion);
            edtSearch.setSelection(edtSearch.getText().length());

            cancelPendingSearch();

            performSearch(suggestion);
            hideKeyboard();
        });

        containerSearchSuggestions.addView(
                item,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private List<String> getSearchHistory() {

        List<String> history = new ArrayList<>();

        if (preferences == null) {
            return history;
        }

        String savedData = preferences.getString(
                KEY_SEARCH_HISTORY,
                "[]"
        );

        try {

            JSONArray jsonArray = new JSONArray(savedData);

            for (int i = 0; i < jsonArray.length(); i++) {

                String item = jsonArray.optString(i, "").trim();

                if (!item.isEmpty() && !history.contains(item)) {
                    history.add(item);
                }
            }

        } catch (Exception e) {
            history.clear();
        }

        return history;
    }

    private void saveSearchHistory(String keyword) {

        if (preferences == null || keyword == null) {
            return;
        }

        String query = keyword.trim();

        if (query.isEmpty()) {
            return;
        }

        List<String> history = getSearchHistory();

        history.removeIf(
                item -> item.equalsIgnoreCase(query)
        );

        history.add(0, query);

        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }

        JSONArray jsonArray = new JSONArray();

        for (String item : history) {
            jsonArray.put(item);
        }

        preferences.edit()
                .putString(KEY_SEARCH_HISTORY, jsonArray.toString())
                .apply();
    }

    private boolean isSourceError(String message) {

        if (message == null) {
            return false;
        }

        String normalized = message.toLowerCase(Locale.ROOT);

        return normalized.contains("google books:")
                || normalized.contains("open library:")
                || normalized.contains("network")
                || normalized.contains("internet")
                || normalized.contains("timeout")
                || normalized.contains("kết nối")
                || normalized.contains("http");
    }

    private void showWarning(List<String> warnings) {

        if (tvSearchWarning == null || warnings == null
                || warnings.isEmpty()) {
            hideWarning();
            return;
        }

        StringBuilder message = new StringBuilder(
                "Một số nguồn dữ liệu chưa phản hồi đầy đủ."
        );

        for (String warning : warnings) {

            if (warning != null && !warning.trim().isEmpty()) {
                message.append("\n• ").append(warning);
            }
        }

        tvSearchWarning.setText(message.toString());
        tvSearchWarning.setVisibility(View.VISIBLE);
    }

    private void hideWarning() {

        if (tvSearchWarning != null) {
            tvSearchWarning.setVisibility(View.GONE);
        }
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

        if (story == null || !isAdded() || getView() == null) {
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

        progressSearch.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );

        if (isLoading) {
            rvSearchResults.setVisibility(View.GONE);
            btnRetrySearch.setVisibility(View.GONE);
        }
    }

    private void showMessage(
            String message,
            boolean showRetry
    ) {

        if (tvSearchMessage != null) {

            tvSearchMessage.setText(message);
            tvSearchMessage.setVisibility(View.VISIBLE);
        }

        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }

        if (btnRetrySearch != null) {

            btnRetrySearch.setVisibility(
                    showRetry ? View.VISIBLE : View.GONE
            );
        }
    }

    private void cancelPendingSearch() {

        if (searchRunnable != null) {

            handler.removeCallbacks(searchRunnable);
            searchRunnable = null;
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

        edtSearch.clearFocus();
    }

    private String normalizeText(String input) {

        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(
                input.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        Pattern pattern = Pattern.compile(
                "\\p{InCombiningDiacriticalMarks}+"
        );

        return pattern.matcher(normalized)
                .replaceAll("")
                .replace('đ', 'd');
    }

    private int dpToPx(int dp) {

        if (getContext() == null) {
            return dp;
        }

        return (int) (
                dp * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    public void onDestroyView() {

        cancelPendingSearch();
        handler.removeCallbacksAndMessages(null);
        searchRequestId++;

        super.onDestroyView();
    }
}
