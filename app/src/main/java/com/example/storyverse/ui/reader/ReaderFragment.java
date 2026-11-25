
package com.example.storyverse.ui.reader;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.ChapterAdapter;
import com.example.storyverse.data.local.entity.ChapterEntity;
import com.example.storyverse.data.local.entity.ReadingHistoryEntity;
import com.example.storyverse.data.local.entity.ReadingProgressEntity;
import com.example.storyverse.utils.UserSessionManager;
import com.example.storyverse.viewmodel.ReaderViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.UUID;

public class ReaderFragment extends Fragment {

    private static final String ARG_STORY_ID = "storyId";
    private static final String ARG_STORY_TITLE = "storyTitle";
    private static final String ARG_CHAPTER_ID = "chapterId";

    private static final String PREF_READER_SETTINGS = "reader_settings";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_LINE_SPACING = "line_spacing_extra";

    private static final int PRIMARY_COLOR = Color.parseColor("#7564E8");
    private static final int BUTTON_TEXT_COLOR = Color.WHITE;
    private static final float DISABLED_ALPHA = 0.45f;

    private static final float FONT_SIZE_SMALL = 14f;
    private static final float FONT_SIZE_MEDIUM = 16f;
    private static final float FONT_SIZE_LARGE = 19f;

    private static final float LINE_SPACING_COMPACT = 4f;
    private static final float LINE_SPACING_NORMAL = 8f;
    private static final float LINE_SPACING_RELAXED = 12f;

    private static final int READER_BG_LIGHT = Color.parseColor("#FFFFFF");
    private static final int READER_BG_CREAM = Color.parseColor("#FBF8F1");
    private static final int READER_BG_DARK = Color.parseColor("#202124");

    private static final int READER_TEXT_LIGHT = Color.parseColor("#302E32");
    private static final int READER_TEXT_DARK = Color.parseColor("#F1F1F1");

    private static final int SAVE_POSITION_THRESHOLD = 250;

    private ReaderViewModel readerViewModel;

    private ImageButton btnBack;
    private ImageButton btnChapterList;
    private ImageButton btnReaderSettings;

    private Button btnPreviousChapter;
    private Button btnNextChapter;
    private Button btnReaderRetry;

    private TextView tvReaderTitle;
    private TextView tvChapterNumber;
    private TextView tvChapterTitle;
    private TextView tvChapterContent;
    private TextView tvChapterProgress;
    private TextView tvReaderAttribution;
    private TextView tvReaderStateMessage;

    private ProgressBar progressReading;
    private ScrollView scrollReader;

    private View readerLoadingContainer;
    private View readerStateContainer;
    private View readerContentContainer;

    private SharedPreferences readerPreferences;

    private String currentStoryId;
    private String requestedChapterId;

    private String currentStoryTitle = "Đang đọc truyện";
    private String currentStoryAuthor;
    private String currentStorySource;
    private String currentStorySourceUrl;
    private String currentStoryLicense;
    private String currentUserId;

    private float currentFontSize = FONT_SIZE_MEDIUM;
    private float currentLineSpacingExtra = LINE_SPACING_NORMAL;
    private String currentBackground = "cream";

    private int pendingScrollPosition;
    private int lastSavedPosition = -1;
    private int lastSavedProgress = -1;

    private boolean shouldRestoreScroll;
    private boolean isRestoringScroll;

    private boolean chaptersLoaded;
    private boolean progressLoaded;
    private boolean progressLoadRequested;

    private boolean requestedChapterHandled;

    private boolean canGoPrevious;
    private boolean canGoNext;

    private String lastHistoryChapterId;

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
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        readerPreferences = requireContext().getSharedPreferences(
                PREF_READER_SETTINGS,
                Context.MODE_PRIVATE
        );

        initializeReaderIdentity();
        loadReaderSettings();

        readerViewModel = new ViewModelProvider(this)
                .get(ReaderViewModel.class);

        setupButtonAppearance();
        setupListeners(view);
        observeViewModel();

        tvReaderTitle.setText(currentStoryTitle);

        if (currentStoryId == null || currentStoryId.trim().isEmpty()) {
            showReaderState(
                    "Không tìm thấy mã truyện. Vui lòng quay lại trang thông tin truyện.",
                    false
            );
            return;
        }

        showLoadingState();
        readerViewModel.loadChapters(currentStoryId);
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        btnChapterList = view.findViewById(R.id.btn_chapter_list);
        btnReaderSettings = view.findViewById(R.id.btn_reader_settings);

        btnPreviousChapter = view.findViewById(R.id.btn_previous_chapter);
        btnNextChapter = view.findViewById(R.id.btn_next_chapter);
        btnReaderRetry = view.findViewById(R.id.btn_reader_retry);

        tvReaderTitle = view.findViewById(R.id.tv_reader_title);
        tvChapterNumber = view.findViewById(R.id.tv_chapter_number);
        tvChapterTitle = view.findViewById(R.id.tv_chapter_title);
        tvChapterContent = view.findViewById(R.id.tv_chapter_content);
        tvChapterProgress = view.findViewById(R.id.tv_chapter_progress);
        tvReaderAttribution = view.findViewById(R.id.tv_reader_attribution);
        tvReaderStateMessage = view.findViewById(R.id.tv_reader_state_message);

        progressReading = view.findViewById(R.id.progress_reading);
        scrollReader = view.findViewById(R.id.scroll_reader);

        readerLoadingContainer = view.findViewById(
                R.id.reader_loading_container
        );

        readerStateContainer = view.findViewById(
                R.id.reader_state_container
        );

        readerContentContainer = view.findViewById(
                R.id.reader_content_container
        );
    }

    private void initializeReaderIdentity() {
        Bundle args = getArguments();

        if (args != null) {
            currentStoryId = args.getString(ARG_STORY_ID);

            requestedChapterId = args.getString(ARG_CHAPTER_ID);

            String title = args.getString(ARG_STORY_TITLE);

            if (title != null && !title.trim().isEmpty()) {
                currentStoryTitle = title.trim();
            }

            currentStoryAuthor = args.getString("storyAuthor");
            currentStorySource = args.getString("storySource");
            currentStorySourceUrl = args.getString("storySourceUrl");
            currentStoryLicense = args.getString("storyLicense");
        }

        buildAttribution();

        currentUserId = UserSessionManager.getCurrentUserId(
                requireContext()
        );
    }

    private void buildAttribution() {
        StringBuilder attribution = new StringBuilder();

        if (hasText(currentStoryAuthor)) {
            attribution.append("Tác giả: ")
                    .append(currentStoryAuthor.trim());
        }

        if (hasText(currentStorySource)) {
            appendAttributionLine(
                    attribution,
                    "Nguồn: " + currentStorySource.trim()
            );
        }

        if (hasText(currentStorySourceUrl)) {
            appendAttributionLine(
                    attribution,
                    currentStorySourceUrl.trim()
            );
        }

        if (hasText(currentStoryLicense)) {
            appendAttributionLine(
                    attribution,
                    "Giấy phép: " + currentStoryLicense.trim()
            );
        }

        tvReaderAttribution.setText(attribution.toString());
        tvReaderAttribution.setVisibility(
                attribution.length() == 0 ? View.GONE : View.VISIBLE
        );
    }

    private void appendAttributionLine(
            StringBuilder builder,
            String value
    ) {
        if (builder.length() > 0) {
            builder.append("\n");
        }

        builder.append(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void setupButtonAppearance() {
        ColorStateList tint = ColorStateList.valueOf(PRIMARY_COLOR);

        btnPreviousChapter.setBackgroundTintList(tint);
        btnNextChapter.setBackgroundTintList(tint);

        btnPreviousChapter.setTextColor(BUTTON_TEXT_COLOR);
        btnNextChapter.setTextColor(BUTTON_TEXT_COLOR);

        setButtonState(btnPreviousChapter, false);
        setButtonState(btnNextChapter, false);
    }

    private void setupListeners(View view) {
        btnBack.setOnClickListener(v -> {
            saveCurrentReadingState();

            NavController navController =
                    Navigation.findNavController(view);

            navController.navigateUp();
        });

        btnChapterList.setOnClickListener(v -> showChapterList());

        btnPreviousChapter.setOnClickListener(v -> {
            if (!canGoPrevious || !progressLoaded) {
                return;
            }

            saveCurrentReadingState();
            readerViewModel.previousChapter();
        });

        btnNextChapter.setOnClickListener(v -> {
            if (!canGoNext || !progressLoaded) {
                return;
            }

            saveCurrentReadingState();
            readerViewModel.nextChapter();
        });

        btnReaderSettings.setOnClickListener(v -> showReaderSettings());

        btnReaderRetry.setOnClickListener(v -> {
            progressLoadRequested = false;
            progressLoaded = false;
            chaptersLoaded = false;
            lastSavedPosition = -1;
            lastSavedProgress = -1;

            showLoadingState();
            readerViewModel.retryLoadChapters();
        });

        scrollReader.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    updateReadingProgress();

                    if (!isRestoringScroll) {
                        saveProgressIfNeeded();
                    }
                }
        );
    }

    private void observeViewModel() {
        readerViewModel.getLoading().observe(
                getViewLifecycleOwner(),
                isLoading -> {
                    if (isLoading == null) {
                        return;
                    }

                    if (isLoading) {
                        showLoadingState();
                    } else {
                        readerLoadingContainer.setVisibility(View.GONE);
                    }
                }
        );

        readerViewModel.getEmptyState().observe(
                getViewLifecycleOwner(),
                isEmpty -> {
                    if (isEmpty == null || !isEmpty) {
                        return;
                    }

                    chaptersLoaded = false;
                    progressLoaded = false;

                    updateChapterButtons(null);
                    tvChapterProgress.setText("0 / 0");

                    showReaderState(
                            "Truyện này hiện chưa có chương nào được lưu. "
                                    + "Bạn có thể thử tải lại hoặc quay về trang thông tin truyện.",
                            true
                    );
                }
        );

        readerViewModel.getChapterError().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (!hasText(message)) {
                        return;
                    }

                    chaptersLoaded = false;
                    progressLoaded = false;

                    updateChapterButtons(null);

                    showReaderState(
                            "Không thể tải nội dung truyện.\n" + message,
                            true
                    );
                }
        );

        readerViewModel.getChapters().observe(
                getViewLifecycleOwner(),
                chapters -> {
                    if (chapters == null || chapters.isEmpty()) {
                        chaptersLoaded = false;
                        updateChapterButtons(null);
                        tvChapterProgress.setText("0 / 0");
                        return;
                    }

                    chaptersLoaded = true;

                    if (!progressLoadRequested) {
                        progressLoadRequested = true;
                        progressLoaded = false;

                        readerViewModel.loadProgress(
                                currentUserId,
                                currentStoryId
                        );
                    }

                    updateChapterButtons(chapters);
                }
        );

        readerViewModel.getCurrentChapter().observe(
                getViewLifecycleOwner(),
                chapter -> {
                    if (chapter == null) {
                        return;
                    }

                    displayChapter(chapter);
                    updateChapterProgress();
                }
        );

        readerViewModel.getProgressLoading().observe(
                getViewLifecycleOwner(),
                isLoading -> {
                    if (isLoading == null || isLoading) {
                        return;
                    }

                    if (!chaptersLoaded || progressLoaded) {
                        return;
                    }

                    List<ChapterEntity> chapters =
                            readerViewModel.getChapters().getValue();

                    if (chapters == null || chapters.isEmpty()) {
                        return;
                    }

                    progressLoaded = true;

                    // Ưu tiên chương được chọn từ màn hình Chi tiết truyện.
                    if (!requestedChapterHandled
                            && hasText(requestedChapterId)) {

                        requestedChapterHandled = true;

                        ChapterEntity requestedChapter =
                                findChapterById(
                                        chapters,
                                        requestedChapterId
                                );

                        if (requestedChapter != null) {
                            pendingScrollPosition = 0;
                            shouldRestoreScroll = false;

                            readerViewModel.selectChapter(
                                    requestedChapter
                            );

                            return;
                        }
                    }

                    // Nếu không được chỉ định chương cụ thể,
                    // tiếp tục khôi phục tiến độ đọc đã lưu.
                    ReadingProgressEntity savedProgress =
                            readerViewModel.getReadingProgress().getValue();

                    if (savedProgress == null) {
                        pendingScrollPosition = 0;
                        shouldRestoreScroll = false;

                        readerViewModel.selectChapter(chapters.get(0));
                        return;
                    }

                    ChapterEntity savedChapter = findChapterById(
                            chapters,
                            savedProgress.getChapterId()
                    );

                    if (savedChapter != null) {
                        pendingScrollPosition = Math.max(
                                0,
                                savedProgress.getLastPosition()
                        );

                        shouldRestoreScroll = true;
                        readerViewModel.selectChapter(savedChapter);
                    } else {
                        pendingScrollPosition = 0;
                        shouldRestoreScroll = false;

                        readerViewModel.selectChapter(chapters.get(0));
                    }
                }
        );

        readerViewModel.getError().observe(
                getViewLifecycleOwner(),
                message -> {
                    if (!hasText(message)) {
                        return;
                    }

                    if (chaptersLoaded) {
                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private ChapterEntity findChapterById(
            List<ChapterEntity> chapters,
            String chapterId
    ) {
        if (chapters == null || chapterId == null) {
            return null;
        }

        for (ChapterEntity chapter : chapters) {
            if (chapter != null
                    && chapterId.equals(chapter.getChapterId())) {
                return chapter;
            }
        }

        return null;
    }

    private void showLoadingState() {
        readerStateContainer.setVisibility(View.GONE);
        readerLoadingContainer.setVisibility(View.VISIBLE);
        scrollReader.setVisibility(View.GONE);

        btnReaderRetry.setEnabled(false);

        canGoPrevious = false;
        canGoNext = false;

        updateChapterButtons(null);
    }

    private void showReaderState(
            String message,
            boolean showRetry
    ) {
        readerLoadingContainer.setVisibility(View.GONE);
        scrollReader.setVisibility(View.GONE);
        readerStateContainer.setVisibility(View.VISIBLE);

        tvReaderStateMessage.setText(message);

        btnReaderRetry.setVisibility(
                showRetry ? View.VISIBLE : View.GONE
        );

        btnReaderRetry.setEnabled(showRetry);

        updateChapterButtons(null);
    }

    private void showReaderContent() {
        readerLoadingContainer.setVisibility(View.GONE);
        readerStateContainer.setVisibility(View.GONE);
        scrollReader.setVisibility(View.VISIBLE);
    }

    private void displayChapter(ChapterEntity chapter) {
        if (chapter == null) {
            return;
        }

        lastSavedPosition = -1;
        lastSavedProgress = -1;

        showReaderContent();

        tvReaderTitle.setText(currentStoryTitle);

        tvChapterNumber.setText(
                "CHƯƠNG " + chapter.getChapterNumber()
        );

        tvChapterTitle.setText(
                chapter.getTitle() != null
                        ? chapter.getTitle()
                        : ""
        );

        String content = chapter.getContent();

        if (content == null || content.trim().isEmpty()) {
            tvChapterContent.setText(
                    "Chương này hiện chưa có nội dung được lưu. "
                            + "Hãy kiểm tra nguồn truyện hoặc kết nối Internet."
            );
        } else {
            tvChapterContent.setText(content);
        }

        applyReaderSettings();
        updateChapterProgress();
        saveHistoryIfNeeded(chapter);

        scrollReader.post(() -> {
            if (!isAdded() || scrollReader == null) {
                return;
            }

            isRestoringScroll = true;

            if (shouldRestoreScroll) {
                int contentHeight = 0;

                if (scrollReader.getChildCount() > 0) {
                    contentHeight = scrollReader.getChildAt(0).getHeight();
                }

                int maxScroll = Math.max(
                        0,
                        contentHeight - scrollReader.getHeight()
                );

                int safePosition = Math.min(
                        pendingScrollPosition,
                        maxScroll
                );

                scrollReader.scrollTo(0, Math.max(0, safePosition));

                shouldRestoreScroll = false;
            } else {
                scrollReader.scrollTo(0, 0);
            }

            updateReadingProgress();

            lastSavedPosition = scrollReader.getScrollY();
            lastSavedProgress = calculateReadingProgress();

            isRestoringScroll = false;
        });
    }

    private void showChapterList() {
        if (readerViewModel == null) {
            return;
        }

        List<ChapterEntity> chapters =
                readerViewModel.getChapters().getValue();

        if (chapters == null || chapters.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Chưa có chương nào để hiển thị",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_chapter_list,
                null
        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        dialog.setContentView(sheetView);

        RecyclerView recyclerView =
                sheetView.findViewById(R.id.rv_chapter_list);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        ChapterAdapter adapter = new ChapterAdapter(chapter -> {
            if (chapter == null) {
                return;
            }

            if (progressLoaded) {
                saveCurrentReadingState();
            }

            shouldRestoreScroll = false;
            pendingScrollPosition = 0;

            readerViewModel.selectChapter(chapter);
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);
        adapter.setChapters(chapters);

        TextView subtitle = sheetView.findViewById(
                R.id.tv_chapter_list_subtitle
        );

        if (subtitle != null) {
            subtitle.setText(
                    chapters.size()
                            + " chương · Chọn chương bạn muốn tiếp tục đọc"
            );
        }

        ChapterEntity currentChapter =
                readerViewModel.getCurrentChapter().getValue();

        if (currentChapter != null) {
            adapter.setCurrentChapter(currentChapter.getChapterId());
        }

        dialog.setOnShowListener(ignored -> {
            View sheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );

            if (sheet != null) {
                BottomSheetBehavior<View> behavior =
                        BottomSheetBehavior.from(sheet);

                behavior.setState(
                        BottomSheetBehavior.STATE_EXPANDED
                );

                behavior.setSkipCollapsed(true);
            }
        });

        dialog.show();
    }

    private void showReaderSettings() {
        View sheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_reader_settings,
                null
        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        dialog.setContentView(sheetView);

        RadioGroup rgFontSize =
                sheetView.findViewById(R.id.rg_font_size);

        RadioButton rbFontSmall =
                sheetView.findViewById(R.id.rb_font_small);

        RadioButton rbFontMedium =
                sheetView.findViewById(R.id.rb_font_medium);

        RadioButton rbFontLarge =
                sheetView.findViewById(R.id.rb_font_large);

        RadioGroup rgBackground =
                sheetView.findViewById(R.id.rg_reader_background);

        RadioButton rbBgLight =
                sheetView.findViewById(R.id.rb_bg_light);

        RadioButton rbBgCream =
                sheetView.findViewById(R.id.rb_bg_cream);

        RadioButton rbBgDark =
                sheetView.findViewById(R.id.rb_bg_dark);

        RadioGroup rgLineSpacing =
                sheetView.findViewById(R.id.rg_line_spacing);

        RadioButton rbSpacingCompact =
                sheetView.findViewById(R.id.rb_spacing_compact);

        RadioButton rbSpacingNormal =
                sheetView.findViewById(R.id.rb_spacing_normal);

        RadioButton rbSpacingRelaxed =
                sheetView.findViewById(R.id.rb_spacing_relaxed);

        if (currentFontSize == FONT_SIZE_SMALL) {
            rbFontSmall.setChecked(true);
        } else if (currentFontSize == FONT_SIZE_LARGE) {
            rbFontLarge.setChecked(true);
        } else {
            rbFontMedium.setChecked(true);
        }

        if (currentLineSpacingExtra <= LINE_SPACING_COMPACT) {
            rbSpacingCompact.setChecked(true);
        } else if (currentLineSpacingExtra >= LINE_SPACING_RELAXED) {
            rbSpacingRelaxed.setChecked(true);
        } else {
            rbSpacingNormal.setChecked(true);
        }

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

        rgFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_font_small) {
                currentFontSize = FONT_SIZE_SMALL;
            } else if (checkedId == R.id.rb_font_large) {
                currentFontSize = FONT_SIZE_LARGE;
            } else {
                currentFontSize = FONT_SIZE_MEDIUM;
            }

            applyReaderSettings();
            saveReaderSettings();
        });

        rgLineSpacing.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_spacing_compact) {
                currentLineSpacingExtra = LINE_SPACING_COMPACT;
            } else if (checkedId == R.id.rb_spacing_relaxed) {
                currentLineSpacingExtra = LINE_SPACING_RELAXED;
            } else {
                currentLineSpacingExtra = LINE_SPACING_NORMAL;
            }

            applyReaderSettings();
            saveReaderSettings();
        });

        rgBackground.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_bg_light) {
                currentBackground = "light";
            } else if (checkedId == R.id.rb_bg_dark) {
                currentBackground = "dark";
            } else {
                currentBackground = "cream";
            }

            applyReaderSettings();
            saveReaderSettings();
        });

        dialog.show();
    }

    private void applyReaderSettings() {
        if (tvChapterContent == null
                || scrollReader == null
                || getView() == null) {
            return;
        }

        tvChapterContent.setTextSize(currentFontSize);

        tvChapterContent.setLineSpacing(
                dp(currentLineSpacingExtra),
                1.0f
        );

        int backgroundColor;
        int textColor;
        int chromeColor;
        int headingColor;

        switch (currentBackground) {
            case "light":
                backgroundColor = READER_BG_LIGHT;
                textColor = READER_TEXT_LIGHT;
                chromeColor = Color.WHITE;
                headingColor = Color.parseColor("#202134");
                break;

            case "dark":
                backgroundColor = READER_BG_DARK;
                textColor = READER_TEXT_DARK;
                chromeColor = Color.parseColor("#292A2D");
                headingColor = Color.parseColor("#F1F1F1");
                break;

            default:
                backgroundColor = READER_BG_CREAM;
                textColor = READER_TEXT_LIGHT;
                chromeColor = Color.parseColor("#FFFDF8");
                headingColor = Color.parseColor("#202134");
                break;
        }

        requireView().setBackgroundColor(backgroundColor);
        scrollReader.setBackgroundColor(backgroundColor);
        readerContentContainer.setBackgroundColor(backgroundColor);

        tvChapterContent.setTextColor(textColor);
        tvChapterTitle.setTextColor(headingColor);
        tvReaderTitle.setTextColor(headingColor);

        tvChapterProgress.setTextColor(
                currentBackground.equals("dark")
                        ? Color.parseColor("#C8C5D8")
                        : Color.parseColor("#77758A")
        );

        if (tvReaderAttribution != null) {
            tvReaderAttribution.setTextColor(
                    currentBackground.equals("dark")
                            ? Color.parseColor("#A7A5B3")
                            : Color.parseColor("#77758A")
            );

            tvReaderAttribution.setLinkTextColor(PRIMARY_COLOR);
        }

        tvChapterNumber.setTextColor(PRIMARY_COLOR);

        int iconColor = currentBackground.equals("dark")
                ? Color.WHITE
                : Color.parseColor("#34344A");

        btnBack.setColorFilter(iconColor);
        btnChapterList.setColorFilter(iconColor);
        btnReaderSettings.setColorFilter(iconColor);

        View header = requireView().findViewById(R.id.reader_header);
        View bottomBar = requireView().findViewById(R.id.reader_bottom_bar);

        if (header != null) {
            header.setBackgroundColor(chromeColor);
        }

        if (bottomBar != null) {
            bottomBar.setBackgroundColor(chromeColor);
        }

        View loadingBackground = readerLoadingContainer;
        View stateBackground = readerStateContainer;

        if (loadingBackground != null) {
            loadingBackground.setBackgroundColor(backgroundColor);
        }

        if (stateBackground != null) {
            stateBackground.setBackgroundColor(backgroundColor);
        }
    }

    private float dp(float value) {
        return value * getResources()
                .getDisplayMetrics()
                .density;
    }

    private void saveReaderSettings() {
        if (readerPreferences == null) {
            return;
        }

        readerPreferences.edit()
                .putFloat(KEY_FONT_SIZE, currentFontSize)
                .putFloat(KEY_LINE_SPACING, currentLineSpacingExtra)
                .putString(KEY_BACKGROUND, currentBackground)
                .apply();
    }

    private void loadReaderSettings() {
        if (readerPreferences == null) {
            return;
        }

        currentFontSize = readerPreferences.getFloat(
                KEY_FONT_SIZE,
                FONT_SIZE_MEDIUM
        );

        currentBackground = readerPreferences.getString(
                KEY_BACKGROUND,
                "cream"
        );

        currentLineSpacingExtra = readerPreferences.getFloat(
                KEY_LINE_SPACING,
                LINE_SPACING_NORMAL
        );

        applyReaderSettings();
    }

    private void updateChapterButtons(List<ChapterEntity> chapters) {
        if (chapters == null || chapters.isEmpty()) {
            canGoPrevious = false;
            canGoNext = false;

            setButtonState(btnPreviousChapter, false);
            setButtonState(btnNextChapter, false);
            return;
        }

        ChapterEntity current =
                readerViewModel.getCurrentChapter().getValue();

        if (current == null || current.getChapterId() == null) {
            canGoPrevious = false;
            canGoNext = false;

            setButtonState(btnPreviousChapter, false);
            setButtonState(btnNextChapter, false);
            return;
        }

        int index = findChapterIndex(
                chapters,
                current.getChapterId()
        );

        if (index < 0) {
            canGoPrevious = false;
            canGoNext = false;

            setButtonState(btnPreviousChapter, false);
            setButtonState(btnNextChapter, false);
            return;
        }

        canGoPrevious = index > 0;
        canGoNext = index < chapters.size() - 1;

        setButtonState(btnPreviousChapter, canGoPrevious);
        setButtonState(btnNextChapter, canGoNext);
    }

    private void setButtonState(Button button, boolean enabled) {
        if (button == null) {
            return;
        }

        button.setBackgroundTintList(
                ColorStateList.valueOf(PRIMARY_COLOR)
        );

        button.setTextColor(BUTTON_TEXT_COLOR);
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1.0f : DISABLED_ALPHA);
    }

    private int findChapterIndex(
            List<ChapterEntity> chapters,
            String chapterId
    ) {
        if (chapters == null || chapterId == null) {
            return -1;
        }

        for (int i = 0; i < chapters.size(); i++) {
            ChapterEntity chapter = chapters.get(i);

            if (chapter != null
                    && chapterId.equals(chapter.getChapterId())) {
                return i;
            }
        }

        return -1;
    }

    private void updateChapterProgress() {
        List<ChapterEntity> chapters =
                readerViewModel.getChapters().getValue();

        ChapterEntity current =
                readerViewModel.getCurrentChapter().getValue();

        if (chapters == null || chapters.isEmpty() || current == null) {
            tvChapterProgress.setText("0 / 0");
            updateChapterButtons(chapters);
            return;
        }

        int index = findChapterIndex(
                chapters,
                current.getChapterId()
        );

        if (index < 0) {
            updateChapterButtons(chapters);
            return;
        }

        tvChapterProgress.setText(
                (index + 1) + " / " + chapters.size()
        );

        updateChapterButtons(chapters);
    }

    private int calculateReadingProgress() {
        if (scrollReader == null || scrollReader.getChildCount() == 0) {
            return 0;
        }

        int contentHeight =
                scrollReader.getChildAt(0).getHeight();

        int visibleHeight = scrollReader.getHeight();
        int maxScroll = contentHeight - visibleHeight;

        if (maxScroll <= 0) {
            return 100;
        }

        int currentScroll = scrollReader.getScrollY();

        int progress = (int) (
                currentScroll * 100f / maxScroll
        );

        return Math.max(0, Math.min(100, progress));
    }

    private void updateReadingProgress() {
        if (progressReading != null) {
            progressReading.setProgress(calculateReadingProgress());
        }
    }

    private void saveProgressIfNeeded() {
        if (isRestoringScroll || !progressLoaded) {
            return;
        }

        ChapterEntity chapter =
                readerViewModel.getCurrentChapter().getValue();

        if (chapter == null) {
            return;
        }

        int currentPosition = scrollReader.getScrollY();
        int currentPercent = calculateReadingProgress();

        boolean positionChanged = lastSavedPosition < 0
                || Math.abs(currentPosition - lastSavedPosition)
                >= SAVE_POSITION_THRESHOLD;

        boolean progressChanged = currentPercent != lastSavedProgress;

        if (!positionChanged && !progressChanged) {
            return;
        }

        saveCurrentProgress(chapter, currentPercent);
    }

    private void saveCurrentProgress(
            ChapterEntity chapter,
            int progressPercent
    ) {
        if (!progressLoaded
                || chapter == null
                || chapter.getChapterId() == null
                || currentUserId == null
                || currentStoryId == null
                || scrollReader == null) {
            return;
        }

        int position = scrollReader.getScrollY();

        ReadingProgressEntity progress =
                new ReadingProgressEntity();

        progress.setProgressId(
                currentUserId + "_" + currentStoryId
        );

        progress.setUserId(currentUserId);
        progress.setStoryId(currentStoryId);
        progress.setChapterId(chapter.getChapterId());
        progress.setProgressPercent(progressPercent);
        progress.setLastPosition(position);
        progress.setLastReadAt(System.currentTimeMillis());

        lastSavedPosition = position;
        lastSavedProgress = progressPercent;

        readerViewModel.saveProgress(progress);
    }

    private void saveCurrentReadingState() {
        if (readerViewModel == null
                || scrollReader == null
                || !progressLoaded) {
            return;
        }

        ChapterEntity chapter =
                readerViewModel.getCurrentChapter().getValue();

        if (chapter == null) {
            return;
        }

        saveCurrentProgress(
                chapter,
                calculateReadingProgress()
        );
    }

    private void saveHistoryIfNeeded(ChapterEntity chapter) {
        if (chapter == null
                || chapter.getChapterId() == null
                || currentUserId == null
                || currentStoryId == null) {
            return;
        }

        String chapterId = chapter.getChapterId();

        if (chapterId.equals(lastHistoryChapterId)) {
            return;
        }

        lastHistoryChapterId = chapterId;

        ReadingHistoryEntity history =
                new ReadingHistoryEntity();

        history.setHistoryId(UUID.randomUUID().toString());
        history.setUserId(currentUserId);
        history.setStoryId(currentStoryId);
        history.setChapterId(chapterId);
        history.setReadAt(System.currentTimeMillis());

        readerViewModel.saveHistory(history);
    }

    @Override
    public void onPause() {
        saveCurrentReadingState();
        super.onPause();
    }
}
