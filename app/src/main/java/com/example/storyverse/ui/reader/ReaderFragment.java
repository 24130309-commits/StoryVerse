
package com.example.storyverse.ui.reader;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.adapter.ChapterAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.storyverse.R;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.viewmodel.ReaderViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.UUID;

public class ReaderFragment extends Fragment {

    // Dữ liệu dùng để chạy thử chức năng đọc truyện.
    private static final String DEMO_STORY_ID =
            "story_demo_001";

    private static final String DEMO_USER_ID =
            "demo_user";

    // Màu sắc sử dụng trong giao diện đọc truyện.
    private static final int PRIMARY_COLOR =
            Color.parseColor("#6D5DFB");

    private static final int BUTTON_TEXT_COLOR =
            Color.WHITE;

    private static final float DISABLED_ALPHA =
            0.45f;

    // Các tùy chọn đọc truyện được lưu trên thiết bị.
    private static final String PREF_READER_SETTINGS =
            "reader_settings";

    private static final String KEY_FONT_SIZE =
            "font_size";

    private static final String KEY_BACKGROUND =
            "background";

    private static final float FONT_SIZE_SMALL =
            14f;

    private static final float FONT_SIZE_MEDIUM =
            16f;

    private static final float FONT_SIZE_LARGE =
            19f;

    private static final int READER_BG_LIGHT =
            Color.parseColor("#FFFFFF");

    private static final int READER_BG_CREAM =
            Color.parseColor("#FBF8F1");

    private static final int READER_BG_DARK =
            Color.parseColor("#202124");

    private static final int READER_TEXT_LIGHT =
            Color.parseColor("#302E32");

    private static final int READER_TEXT_DARK =
            Color.parseColor("#F1F1F1");

    private ReaderViewModel readerViewModel;

    // Các thành phần hiển thị trên màn hình đọc truyện.
    private ImageButton btnBack;
    private ImageButton btnChapterList;
    private ImageButton btnReaderSettings;
    private Button btnPreviousChapter;
    private Button btnNextChapter;
    private TextView tvReaderTitle;
    private TextView tvChapterNumber;
    private TextView tvChapterTitle;
    private TextView tvChapterContent;
    private TextView tvChapterProgress;
    private ProgressBar progressReading;
    private ScrollView scrollReader;

    // Ghi nhớ cỡ chữ và màu nền mà người đọc đã chọn.
    private SharedPreferences readerPreferences;

    private float currentFontSize =
            FONT_SIZE_MEDIUM;

    private String currentBackground =
            "cream";

    // Thông tin cần thiết để lưu và khôi phục vị trí đọc.
    private int pendingScrollPosition = 0;

    private boolean shouldRestoreScroll = false;

    private boolean isRestoringScroll = false;

    private boolean chaptersLoaded = false;

    private boolean progressLoaded = false;

    private boolean progressLoadRequested = false;

    private int lastSavedProgress = -1;

    private String lastHistoryChapterId = null;

    // Kiểm tra xem có thể chuyển sang chương trước hoặc sau hay không.
    private boolean canGoPrevious = false;

    private boolean canGoNext = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_reader,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        // Khởi tạo các thành phần giao diện.
        initViews(view);

        // Lấy lại cài đặt đọc truyện đã lưu trước đó.
        readerPreferences =
                requireContext().getSharedPreferences(
                        PREF_READER_SETTINGS,
                        Context.MODE_PRIVATE
                );

        loadReaderSettings();

        // Kết nối ViewModel để xử lý dữ liệu đọc truyện.
        readerViewModel =
                new ViewModelProvider(this)
                        .get(ReaderViewModel.class);

        setupButtonAppearance();
        setupListeners(view);
        observeViewModel();

        // Thêm dữ liệu chương mẫu để kiểm tra giao diện đọc.
        readerViewModel.insertSampleChapters(
                DEMO_STORY_ID
        );
    }

    private void initViews(View view) {

        btnBack =
                view.findViewById(
                        R.id.btn_back
                );

        // Nút ba gạch dùng để mở danh sách chương.
        btnChapterList =
                view.findViewById(
                        R.id.btn_chapter_list
                );

        btnReaderSettings =
                view.findViewById(
                        R.id.btn_reader_settings
                );

        btnPreviousChapter =
                view.findViewById(
                        R.id.btn_previous_chapter
                );

        btnNextChapter =
                view.findViewById(
                        R.id.btn_next_chapter
                );

        tvReaderTitle =
                view.findViewById(
                        R.id.tv_reader_title
                );

        tvChapterNumber =
                view.findViewById(
                        R.id.tv_chapter_number
                );

        tvChapterTitle =
                view.findViewById(
                        R.id.tv_chapter_title
                );

        tvChapterContent =
                view.findViewById(
                        R.id.tv_chapter_content
                );

        tvChapterProgress =
                view.findViewById(
                        R.id.tv_chapter_progress
                );

        progressReading =
                view.findViewById(
                        R.id.progress_reading
                );

        scrollReader =
                view.findViewById(
                        R.id.scroll_reader
                );
    }

    private void setupButtonAppearance() {

        ColorStateList buttonTint =
                ColorStateList.valueOf(
                        PRIMARY_COLOR
                );

        btnPreviousChapter.setBackgroundTintList(
                buttonTint
        );

        btnNextChapter.setBackgroundTintList(
                buttonTint
        );

        btnPreviousChapter.setTextColor(
                BUTTON_TEXT_COLOR
        );

        btnNextChapter.setTextColor(
                BUTTON_TEXT_COLOR
        );

        setButtonState(
                btnPreviousChapter,
                false
        );

        setButtonState(
                btnNextChapter,
                false
        );
    }

    private void setupListeners(View view) {

        // Quay lại màn hình trước và lưu vị trí đang đọc.
        btnBack.setOnClickListener(v -> {

            saveCurrentReadingState();

            NavController navController =
                    Navigation.findNavController(view);

            navController.navigateUp();
        });

        // Mở danh sách chương khi nhấn nút tương ứng.
        btnChapterList.setOnClickListener(
                v -> showChapterList()
        );

        // Chuyển về chương trước nếu vẫn còn chương để chuyển.
        btnPreviousChapter.setOnClickListener(v -> {

            if (!canGoPrevious) {
                return;
            }

            ChapterEntity currentChapter =
                    readerViewModel
                            .getCurrentChapter()
                            .getValue();

            if (currentChapter == null) {
                return;
            }

            saveCurrentReadingState();

            readerViewModel.previousChapter();
        });

        // Chuyển sang chương tiếp theo.
        btnNextChapter.setOnClickListener(v -> {

            if (!canGoNext) {
                return;
            }

            ChapterEntity currentChapter =
                    readerViewModel
                            .getCurrentChapter()
                            .getValue();

            if (currentChapter == null) {
                return;
            }

            saveCurrentReadingState();

            readerViewModel.nextChapter();
        });

        // Mở phần cài đặt giao diện đọc truyện.
        btnReaderSettings.setOnClickListener(
                v -> showReaderSettings()
        );

        // Cập nhật tiến độ mỗi khi người đọc cuộn nội dung.
        scrollReader.setOnScrollChangeListener(
                (v,
                 scrollX,
                 scrollY,
                 oldScrollX,
                 oldScrollY) -> {

                    if (isRestoringScroll) {

                        updateReadingProgress();

                        return;
                    }

                    updateReadingProgress();

                    saveProgressIfNeeded();
                }
        );
    }

    private void showChapterList() {

        View bottomSheetView =
                getLayoutInflater().inflate(
                        R.layout.bottom_sheet_chapter_list,
                        null
                );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        dialog.setContentView(
                bottomSheetView
        );

        RecyclerView recyclerView =
                bottomSheetView.findViewById(
                        R.id.rv_chapter_list
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        // Xử lý khi người đọc chọn một chương trong danh sách.
        ChapterAdapter adapter =
                new ChapterAdapter(chapter -> {

                    if (chapter == null) {
                        return;
                    }

                    // Lưu vị trí của chương đang đọc trước khi chuyển.
                    saveCurrentReadingState();

                    readerViewModel.selectChapter(
                            chapter
                    );

                    dialog.dismiss();
                });

        recyclerView.setAdapter(
                adapter
        );

        // Lấy danh sách chương đã được tải từ ViewModel.
        List<ChapterEntity> chapters =
                readerViewModel
                        .getChapters()
                        .getValue();

        adapter.setChapters(
                chapters
        );

        // Đánh dấu chương hiện tại trong danh sách.
        ChapterEntity currentChapter =
                readerViewModel
                        .getCurrentChapter()
                        .getValue();

        if (currentChapter != null) {

            adapter.setCurrentChapter(
                    currentChapter.getChapterId()
            );
        }

        if (chapters == null ||
                chapters.isEmpty()) {

            android.widget.Toast.makeText(
                    requireContext(),
                    "Chưa có chương nào để hiển thị",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
        }

        dialog.show();
    }

    private void showReaderSettings() {

        View bottomSheetView =
                getLayoutInflater()
                        .inflate(
                                R.layout.bottom_sheet_reader_settings,
                                null
                        );

        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(
                        requireContext()
                );

        bottomSheetDialog.setContentView(
                bottomSheetView
        );

        // Các lựa chọn cỡ chữ.
        RadioGroup rgFontSize =
                bottomSheetView.findViewById(
                        R.id.rg_font_size
                );

        RadioButton rbFontSmall =
                bottomSheetView.findViewById(
                        R.id.rb_font_small
                );

        RadioButton rbFontMedium =
                bottomSheetView.findViewById(
                        R.id.rb_font_medium
                );

        RadioButton rbFontLarge =
                bottomSheetView.findViewById(
                        R.id.rb_font_large
                );

        // Các lựa chọn màu nền.
        RadioGroup rgBackground =
                bottomSheetView.findViewById(
                        R.id.rg_reader_background
                );

        RadioButton rbBgLight =
                bottomSheetView.findViewById(
                        R.id.rb_bg_light
                );

        RadioButton rbBgCream =
                bottomSheetView.findViewById(
                        R.id.rb_bg_cream
                );

        RadioButton rbBgDark =
                bottomSheetView.findViewById(
                        R.id.rb_bg_dark
                );

        // Hiển thị lại cỡ chữ đã chọn ở lần đọc trước.
        if (currentFontSize ==
                FONT_SIZE_SMALL) {

            rbFontSmall.setChecked(true);

        } else if (
                currentFontSize ==
                        FONT_SIZE_LARGE
        ) {

            rbFontLarge.setChecked(true);

        } else {

            rbFontMedium.setChecked(true);
        }

        // Hiển thị lại màu nền đã chọn.
        switch (currentBackground) {

            case "light":

                rbBgLight.setChecked(true);

                break;

            case "dark":

                rbBgDark.setChecked(true);

                break;

            default:

                rbBgCream.setChecked(true);

                break;
        }

        // Thay đổi cỡ chữ ngay khi người đọc chọn.
        rgFontSize.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId ==
                            R.id.rb_font_small) {

                        currentFontSize =
                                FONT_SIZE_SMALL;

                    } else if (
                            checkedId ==
                                    R.id.rb_font_large
                    ) {

                        currentFontSize =
                                FONT_SIZE_LARGE;

                    } else {

                        currentFontSize =
                                FONT_SIZE_MEDIUM;
                    }

                    applyReaderSettings();

                    saveReaderSettings();
                }
        );

        // Thay đổi màu nền theo lựa chọn của người đọc.
        rgBackground.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId ==
                            R.id.rb_bg_light) {

                        currentBackground =
                                "light";

                    } else if (
                            checkedId ==
                                    R.id.rb_bg_dark
                    ) {

                        currentBackground =
                                "dark";

                    } else {

                        currentBackground =
                                "cream";
                    }

                    applyReaderSettings();

                    saveReaderSettings();
                }
        );

        bottomSheetDialog.show();
    }

    private void applyReaderSettings() {

        tvChapterContent.setTextSize(
                currentFontSize
        );

        int backgroundColor;

        int textColor;

        switch (currentBackground) {

            case "light":

                backgroundColor =
                        READER_BG_LIGHT;

                textColor =
                        READER_TEXT_LIGHT;

                break;

            case "dark":

                backgroundColor =
                        READER_BG_DARK;

                textColor =
                        READER_TEXT_DARK;

                break;

            default:

                backgroundColor =
                        READER_BG_CREAM;

                textColor =
                        READER_TEXT_LIGHT;

                break;
        }

        requireView().setBackgroundColor(
                backgroundColor
        );

        scrollReader.setBackgroundColor(
                backgroundColor
        );

        tvChapterContent.setTextColor(
                textColor
        );
    }

    // Lưu cỡ chữ và màu nền để lần sau không phải chọn lại.
    private void saveReaderSettings() {

        if (readerPreferences == null) {
            return;
        }

        readerPreferences
                .edit()
                .putFloat(
                        KEY_FONT_SIZE,
                        currentFontSize
                )
                .putString(
                        KEY_BACKGROUND,
                        currentBackground
                )
                .apply();
    }

    // Đọc lại cài đặt đã lưu trên thiết bị.
    private void loadReaderSettings() {

        if (readerPreferences == null) {
            return;
        }

        currentFontSize =
                readerPreferences.getFloat(
                        KEY_FONT_SIZE,
                        FONT_SIZE_MEDIUM
                );

        currentBackground =
                readerPreferences.getString(
                        KEY_BACKGROUND,
                        "cream"
                );

        if (tvChapterContent != null) {

            applyReaderSettings();
        }
    }

    private void observeViewModel() {

        // Theo dõi danh sách chương sau khi tải dữ liệu.
        readerViewModel
                .getChapters()
                .observe(
                        getViewLifecycleOwner(),
                        chapters -> {

                            if (chapters == null ||
                                    chapters.isEmpty()) {

                                chaptersLoaded = false;

                                updateChapterButtons(null);

                                return;
                            }

                            chaptersLoaded = true;

                            updateChapterProgress();

                            // Chỉ tải tiến độ đọc một lần khi đã có danh sách chương.
                            if (!progressLoadRequested) {

                                progressLoadRequested = true;

                                readerViewModel.loadProgress(
                                        DEMO_USER_ID,
                                        DEMO_STORY_ID
                                );
                            }

                            if (progressLoaded &&
                                    readerViewModel
                                            .getCurrentChapter()
                                            .getValue() == null) {

                                readerViewModel.selectChapter(
                                        chapters.get(0)
                                );
                            }
                        }
                );

        // Hiển thị nội dung khi chương hiện tại thay đổi.
        readerViewModel
                .getCurrentChapter()
                .observe(
                        getViewLifecycleOwner(),
                        this::displayChapter
                );

        // Kiểm tra tiến độ cũ để khôi phục vị trí đọc.
        readerViewModel
                .getReadingProgress()
                .observe(
                        getViewLifecycleOwner(),
                        progress -> {

                            if (progressLoaded) {
                                return;
                            }

                            if (!chaptersLoaded) {
                                return;
                            }

                            progressLoaded = true;

                            List<ChapterEntity> chapters =
                                    readerViewModel
                                            .getChapters()
                                            .getValue();

                            if (chapters == null ||
                                    chapters.isEmpty()) {

                                return;
                            }

                            // Nếu chưa có tiến độ thì bắt đầu từ chương đầu tiên.
                            if (progress == null) {

                                pendingScrollPosition = 0;

                                shouldRestoreScroll = false;

                                readerViewModel.selectChapter(
                                        chapters.get(0)
                                );

                                return;
                            }

                            // Lấy lại vị trí đọc đã lưu trước đó.
                            pendingScrollPosition =
                                    Math.max(
                                            0,
                                            progress.getLastPosition()
                                    );

                            shouldRestoreScroll = true;

                            String savedChapterId =
                                    progress.getChapterId();

                            ChapterEntity savedChapter = null;

                            for (ChapterEntity chapter : chapters) {

                                if (chapter != null &&
                                        chapter.getChapterId() != null &&
                                        chapter.getChapterId().equals(
                                                savedChapterId
                                        )) {

                                    savedChapter = chapter;

                                    break;
                                }
                            }

                            // Nếu chương đã lưu vẫn còn thì mở lại chương đó.
                            if (savedChapter != null) {

                                readerViewModel.selectChapter(
                                        savedChapter
                                );

                            } else {

                                // Nếu không tìm thấy chương cũ thì quay về chương đầu.
                                pendingScrollPosition = 0;

                                shouldRestoreScroll = false;

                                readerViewModel.selectChapter(
                                        chapters.get(0)
                                );
                            }
                        }
                );

        // Hiện tại chưa có giao diện loading riêng cho màn hình đọc.
        readerViewModel
                .getLoading()
                .observe(
                        getViewLifecycleOwner(),
                        isLoading -> {

                            if (isLoading == null) {
                                return;
                            }

                            /*
                             * Chưa xử lý Loading UI ở đây.
                             *
                             * progressReading là thanh
                             * tiến độ đọc, không phải loading.
                             */
                        }
                );

        // Thông báo khi quá trình tải hoặc xử lý dữ liệu gặp lỗi.
        readerViewModel
                .getError()
                .observe(
                        getViewLifecycleOwner(),
                        message -> {

                            if (message != null &&
                                    !message.isEmpty()) {

                                android.widget.Toast.makeText(
                                        requireContext(),
                                        message,
                                        android.widget.Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void displayChapter(
            ChapterEntity chapter
    ) {

        if (chapter == null) {
            return;
        }

        lastSavedProgress = -1;

        // Cập nhật thông tin chương đang được chọn.
        tvReaderTitle.setText(
                "Đang đọc truyện"
        );

        tvChapterNumber.setText(
                "CHƯƠNG " +
                        chapter.getChapterNumber()
        );

        tvChapterTitle.setText(
                chapter.getTitle()
        );

        tvChapterContent.setText(
                chapter.getContent()
        );

        applyReaderSettings();

        updateChapterProgress();

        // Ghi nhận chương vừa mở vào lịch sử đọc.
        saveHistoryIfNeeded(chapter);

        // Đưa nội dung về vị trí đã lưu hoặc về đầu chương mới.
        scrollReader.post(() -> {

            isRestoringScroll = true;

            if (shouldRestoreScroll) {

                int contentHeight =
                        scrollReader
                                .getChildAt(0)
                                .getHeight();

                int visibleHeight =
                        scrollReader.getHeight();

                int maxScroll =
                        Math.max(
                                0,
                                contentHeight - visibleHeight
                        );

                int safePosition =
                        Math.max(
                                0,
                                Math.min(
                                        pendingScrollPosition,
                                        maxScroll
                                )
                        );

                scrollReader.scrollTo(
                        0,
                        safePosition
                );

                shouldRestoreScroll = false;

            } else {

                scrollReader.scrollTo(0, 0);
            }

            updateReadingProgress();

            isRestoringScroll = false;
        });
    }

    // Cập nhật trạng thái hai nút chuyển chương.
    private void updateChapterButtons(
            List<ChapterEntity> chapters
    ) {

        if (chapters == null ||
                chapters.isEmpty()) {

            canGoPrevious = false;

            canGoNext = false;

            setButtonState(btnPreviousChapter, false);

            setButtonState(btnNextChapter, false);

            return;
        }

        ChapterEntity current =
                readerViewModel
                        .getCurrentChapter()
                        .getValue();

        if (current == null ||
                current.getChapterId() == null) {

            canGoPrevious = false;

            canGoNext = false;

            setButtonState(btnPreviousChapter, false);

            setButtonState(btnNextChapter, false);

            return;
        }

        int currentIndex =
                findChapterIndex(
                        chapters,
                        current.getChapterId()
                );

        if (currentIndex < 0) {

            canGoPrevious = false;

            canGoNext = false;

            setButtonState(btnPreviousChapter, false);

            setButtonState(btnNextChapter, false);

            return;
        }

        canGoPrevious = currentIndex > 0;

        canGoNext =
                currentIndex < chapters.size() - 1;

        setButtonState(
                btnPreviousChapter,
                canGoPrevious
        );

        setButtonState(
                btnNextChapter,
                canGoNext
        );
    }

    private void setButtonState(
            Button button,
            boolean enabled
    ) {

        if (button == null) {
            return;
        }

        button.setBackgroundTintList(
                ColorStateList.valueOf(PRIMARY_COLOR)
        );

        button.setTextColor(
                BUTTON_TEXT_COLOR
        );

        button.setEnabled(enabled);

        button.setAlpha(
                enabled ? 1.0f : DISABLED_ALPHA
        );
    }

    // Tìm vị trí của chương trong danh sách.
    private int findChapterIndex(
            List<ChapterEntity> chapters,
            String chapterId
    ) {

        if (chapters == null ||
                chapterId == null) {

            return -1;
        }

        for (int i = 0; i < chapters.size(); i++) {

            ChapterEntity chapter = chapters.get(i);

            if (chapter != null &&
                    chapter.getChapterId() != null &&
                    chapterId.equals(chapter.getChapterId())) {

                return i;
            }
        }

        return -1;
    }

    // Hiển thị số thứ tự chương hiện tại trên tổng số chương.
    private void updateChapterProgress() {

        List<ChapterEntity> chapters =
                readerViewModel
                        .getChapters()
                        .getValue();

        ChapterEntity current =
                readerViewModel
                        .getCurrentChapter()
                        .getValue();

        if (chapters == null ||
                chapters.isEmpty() ||
                current == null) {

            updateChapterButtons(chapters);

            return;
        }

        int currentIndex =
                findChapterIndex(
                        chapters,
                        current.getChapterId()
                );

        if (currentIndex < 0) {

            updateChapterButtons(chapters);

            return;
        }

        int currentNumber = currentIndex + 1;

        int total = chapters.size();

        tvChapterProgress.setText(
                currentNumber +
                        " / " +
                        total
        );

        updateChapterButtons(chapters);
    }

    // Tính phần trăm nội dung đã đọc dựa trên vị trí cuộn.
    private int calculateReadingProgress() {

        if (scrollReader.getChildCount() == 0) {
            return 0;
        }

        int contentHeight =
                scrollReader
                        .getChildAt(0)
                        .getHeight();

        int visibleHeight =
                scrollReader.getHeight();

        int maxScroll =
                contentHeight - visibleHeight;

        if (maxScroll <= 0) {
            return 100;
        }

        int currentScroll =
                scrollReader.getScrollY();

        int progress =
                (int) (
                        currentScroll * 100f / maxScroll
                );

        return Math.max(
                0,
                Math.min(100, progress)
        );
    }

    private void updateReadingProgress() {

        int progress =
                calculateReadingProgress();

        progressReading.setProgress(progress);
    }

    // Chỉ lưu lại khi phần trăm tiến độ có thay đổi.
    private void saveProgressIfNeeded() {

        if (isRestoringScroll) {
            return;
        }

        ChapterEntity currentChapter =
                readerViewModel
                        .getCurrentChapter()
                        .getValue();

        if (currentChapter == null) {
            return;
        }

        int progress =
                calculateReadingProgress();

        if (progress == lastSavedProgress) {
            return;
        }

        saveCurrentProgress(
                currentChapter,
                progress
        );
    }

    // Lưu chương hiện tại cùng phần trăm và vị trí cuộn.
    private void saveCurrentProgress(
            ChapterEntity chapter,
            int progressPercent
    ) {

        if (chapter == null) {
            return;
        }

        int lastPosition =
                scrollReader.getScrollY();

        ReadingProgressEntity progress =
                new ReadingProgressEntity();

        progress.setProgressId(
                DEMO_USER_ID +
                        "_" +
                        DEMO_STORY_ID
        );

        progress.setUserId(DEMO_USER_ID);

        progress.setStoryId(DEMO_STORY_ID);

        progress.setChapterId(
                chapter.getChapterId()
        );

        progress.setProgressPercent(
                progressPercent
        );

        progress.setLastPosition(
                lastPosition
        );

        progress.setLastReadAt(
                System.currentTimeMillis()
        );

        lastSavedProgress = progressPercent;

        readerViewModel.saveProgress(progress);
    }

    // Lưu trạng thái hiện tại trước khi rời màn hình đọc.
    private void saveCurrentReadingState() {

        ChapterEntity currentChapter =
                readerViewModel
                        .getCurrentChapter()
                        .getValue();

        if (currentChapter == null) {
            return;
        }

        int progress =
                calculateReadingProgress();

        saveCurrentProgress(
                currentChapter,
                progress
        );
    }

    // Lưu lịch sử khi người đọc mở một chương mới.
    private void saveHistoryIfNeeded(
            ChapterEntity chapter
    ) {

        if (chapter == null) {
            return;
        }

        String chapterId =
                chapter.getChapterId();

        if (chapterId == null) {
            return;
        }

        if (chapterId.equals(lastHistoryChapterId)) {
            return;
        }

        lastHistoryChapterId = chapterId;

        ReadingHistoryEntity history =
                new ReadingHistoryEntity();

        history.setHistoryId(
                UUID.randomUUID().toString()
        );

        history.setUserId(DEMO_USER_ID);

        history.setStoryId(DEMO_STORY_ID);

        history.setChapterId(chapterId);

        history.setReadAt(
                System.currentTimeMillis()
        );

        readerViewModel.saveHistory(history);
    }

    @Override
    public void onPause() {

        // Lưu tiến độ trước khi màn hình tạm dừng.
        saveCurrentReadingState();

        super.onPause();
    }
}
