
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
import com.example.storyverse.data.model.Story;
import com.example.storyverse.data.repository.MultiSourceStoryRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressSearch;
    private TextView tvSearchMessage;

    private MultiSourceStoryRepository multiSourceRepository;
    private StoryAdapter storyAdapter;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private Runnable searchRunnable;

    private final List<Story> searchResults = new ArrayList<>();

    // Tránh xử lý kết quả của từ khóa cũ.
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

        showMessage("Nhập tên truyện hoặc tác giả để tìm kiếm");
    }

    private void initViews(View view) {

        edtSearch = view.findViewById(R.id.edt_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressSearch = view.findViewById(R.id.progress_search);
        tvSearchMessage = view.findViewById(R.id.tv_search_message);
    }

    private void initRepositories() {

        multiSourceRepository = new MultiSourceStoryRepository();
    }

    // Khởi tạo RecyclerView và StoryAdapter.
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
                    public void onLocalStoryClick(
                            com.example.storyverse.data.local.entity.StoryEntity story
                    ) {
                        // Sẽ tích hợp điều hướng truyện trong thư viện sau.
                    }
                }
        );

        rvSearchResults.setAdapter(storyAdapter);
    }

    // Mở màn hình chi tiết truyện.
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

    // Thiết lập chức năng tìm kiếm.
    private void setupSearch() {

        edtSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // Tìm kiếm khi nhấn nút Search trên bàn phím.
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                String keyword = edtSearch.getText()
                        .toString()
                        .trim();

                if (!keyword.isEmpty()) {

                    if (searchRunnable != null) {
                        handler.removeCallbacks(searchRunnable);
                    }

                    performSearch(keyword);
                }

                hideKeyboard();

                return true;
            }

            return false;
        });

        // Tự động tìm kiếm khi người dùng ngừng nhập.
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

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                String keyword = s.toString().trim();

                if (keyword.isEmpty()) {

                    searchRequestId++;

                    searchResults.clear();

                    if (storyAdapter != null) {
                        storyAdapter.setApiStories(searchResults);
                    }

                    showLoading(false);

                    showMessage(
                            "Nhập tên truyện hoặc tác giả để tìm kiếm"
                    );

                    return;
                }

                // Chờ người dùng ngừng nhập 600ms.
                searchRunnable = () -> performSearch(keyword);

                handler.postDelayed(searchRunnable, 600);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Gọi API tìm kiếm truyện.
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

                        // Không tìm thấy truyện.
                        if (searchResults.isEmpty()) {

                            storyAdapter.setApiStories(searchResults);

                            showMessage(
                                    "Không tìm thấy truyện phù hợp"
                            );

                            return;
                        }

                        // Cập nhật danh sách truyện.
                        storyAdapter.setApiStories(searchResults);

                        tvSearchMessage.setVisibility(View.GONE);

                        rvSearchResults.setVisibility(View.VISIBLE);

                        android.util.Log.d(
                                "SearchFragment",
                                "Tìm thấy "
                                        + searchResults.size()
                                        + " truyện"
                        );

                        // Ghi nhận cảnh báo từ các nguồn API.
                        if (warnings != null && !warnings.isEmpty()) {

                            android.util.Log.w(
                                    "SearchFragment",
                                    "Cảnh báo API: " + warnings
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {

                        if (!isAdded()
                                || currentRequestId != searchRequestId) {
                            return;
                        }

                        showLoading(false);

                        storyAdapter.setApiStories(
                                new ArrayList<>()
                        );

                        showMessage(
                                message != null
                                        ? message
                                        : "Đã xảy ra lỗi khi tìm kiếm"
                        );
                    }
                }
        );
    }

    // Hiển thị hoặc ẩn vòng tròn tải.
    private void showLoading(boolean isLoading) {

        if (progressSearch != null) {

            progressSearch.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE
            );
        }
    }

    // Hiển thị thông báo tìm kiếm.
    private void showMessage(String message) {

        if (tvSearchMessage != null) {

            tvSearchMessage.setText(message);

            tvSearchMessage.setVisibility(View.VISIBLE);
        }
    }

    // Ẩn bàn phím.
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

    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);

        searchRequestId++;

        super.onDestroyView();
    }
}
