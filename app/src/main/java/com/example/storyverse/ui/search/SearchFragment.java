package com.example.storyverse.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.StoryAdapter;
import com.example.storyverse.data.model.Story;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {

    private EditText etSearchQuery;
    private ImageView btnClearSearch;
    private ChipGroup chipGroupSearchCategory;
    private Chip chipSearchAll;
    private TextView tvSearchResultTitle;
    private RecyclerView rvSearchResults;
    private TextView tvEmptySearch;

    private StoryAdapter storyAdapter;
    private final List<Story> allStories = new ArrayList<>();
    private final List<Story> filteredStories = new ArrayList<>();

    private String currentQuery = "";
    private String selectedCategory = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Đảm bảo tên file layout khớp với file XML của bạn (vd: fragment_search.xml)
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
//        loadData();
        setupListeners();
    }

    private void initViews(@NonNull View view) {
        etSearchQuery = view.findViewById(R.id.etSearchQuery);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        chipGroupSearchCategory = view.findViewById(R.id.chipGroupSearchCategory);
        chipSearchAll = view.findViewById(R.id.chipSearchAll);
        tvSearchResultTitle = view.findViewById(R.id.tvSearchResultTitle);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        tvEmptySearch = view.findViewById(R.id.tvEmptySearch);
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Khởi tạo Adapter (truyền danh sách kết quả và sự kiện click vào item nếu có)
        storyAdapter = new StoryAdapter(requireContext(), filteredStories, story -> {
            // TODO: Xử lý mở màn hình chi tiết truyện khi người dùng nhấn vào item
        });
        rvSearchResults.setAdapter(storyAdapter);
    }

    private void setupListeners() {
        // 1. Lắng nghe thay đổi nội dung ô tìm kiếm theo thời gian thực
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                btnClearSearch.setVisibility(currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
                filterStories();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 2. Xử lý khi nhấn nút Search trên bàn phím ảo
        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                etSearchQuery.clearFocus();
                return true;
            }
            return false;
        });

        // 3. Nút xóa nhanh từ khóa
        btnClearSearch.setOnClickListener(v -> {
            etSearchQuery.setText("");
            etSearchQuery.requestFocus();
        });

        // 4. Lọc theo thể loại khi chọn Chip
        chipGroupSearchCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Nếu người dùng bỏ chọn chip hiện tại, tự động chọn lại "Tất cả"
                chipSearchAll.setChecked(true);
                selectedCategory = "Tất cả";
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    selectedCategory = selectedChip.getText().toString();
                }
            }
            filterStories();
        });
    }

    private void filterStories() {
        filteredStories.clear();
        String normalizedQuery = removeAccents(currentQuery.toLowerCase(Locale.ROOT));

        for (Story story : allStories) {
            boolean matchesCategory = selectedCategory.equals("Tất cả")
                    || (story.getCategory() != null && story.getCategory().equalsIgnoreCase(selectedCategory));

            boolean matchesQuery = true;
            if (!normalizedQuery.isEmpty()) {
                String title = removeAccents(safeString(story.getTitle()).toLowerCase(Locale.ROOT));
                String author = removeAccents(safeString(story.getAuthor()).toLowerCase(Locale.ROOT));
                matchesQuery = title.contains(normalizedQuery) || author.contains(normalizedQuery);
            }

            if (matchesCategory && matchesQuery) {
                filteredStories.add(story);
            }
        }

        updateUIState();
    }

    private void updateUIState() {
        // Cập nhật tiêu đề danh sách
        if (currentQuery.isEmpty() && selectedCategory.equals("Tất cả")) {
            tvSearchResultTitle.setText("Gợi ý tìm kiếm");
        } else {
            tvSearchResultTitle.setText(String.format(Locale.ROOT, "Kết quả tìm kiếm (%d)", filteredStories.size()));
        }

        // Ẩn/hiện RecyclerView hoặc thông báo trống
        if (filteredStories.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            tvEmptySearch.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            tvEmptySearch.setVisibility(View.GONE);
        }

        storyAdapter.notifyDataSetChanged();
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearchQuery != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
            }
        }
    }

    /**
     * Chuẩn hóa tiếng Việt không dấu giúp tìm kiếm linh hoạt hơn (vd: "tien hiep" -> khớp "Tiên Hiệp")
     */
    private String removeAccents(String input) {
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }
}