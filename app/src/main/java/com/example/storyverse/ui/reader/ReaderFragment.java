
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
import com.example.storyverse.viewmodel.ReaderViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;

public class ReaderFragment extends Fragment {

    private static final String DEFAULT_STORY_ID = "story_001";
    private static final String ARG_STORY_ID = "storyId";
    private static final String KEY_GUEST_USER_ID = "guest_user_id";

    private static final int PRIMARY_COLOR = Color.parseColor("#6D5DFB");
    private static final int BUTTON_TEXT_COLOR = Color.WHITE;
    private static final float DISABLED_ALPHA = 0.45f;

    private static final String PREF_READER_SETTINGS = "reader_settings";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_LINE_SPACING = "line_spacing_extra";
    private static final String ARG_STORY_TITLE = "storyTitle";

    private static final float FONT_SIZE_SMALL = 14f;
    private static final float FONT_SIZE_MEDIUM = 16f;
    private static final float FONT_SIZE_LARGE = 19f;

    private static final float LINE_SPACING_COMPACT = 4f;
    private static final float LINE_SPACING_NORMAL = 8f;
    private static final float LINE_SPACING_RELAXED = 12f;

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
    private TextView tvReaderAttribution;

    private ProgressBar progressReading;
    private ScrollView scrollReader;

    private SharedPreferences readerPreferences;

    private String currentStoryId;
    private String currentStoryTitle = "Đang đọc truyện";
    private String currentStoryAuthor;
    private String currentStorySource;
    private String currentStorySourceUrl;
    private String currentStoryLicense;
    private String currentUserId;

    private float currentFontSize = FONT_SIZE_MEDIUM;
    private float currentLineSpacingExtra = LINE_SPACING_NORMAL;
    private String currentBackground = "cream";

    private int pendingScrollPosition = 0;

    private boolean shouldRestoreScroll = false;
    private boolean isRestoringScroll = false;

    private boolean chaptersLoaded = false;
    private boolean progressLoaded = false;
    private boolean progressLoadRequested = false;

    private int lastSavedProgress = -1;

    private String lastHistoryChapterId = null;

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

        readerViewModel.loadChapters(currentStoryId);
    }

    private void initializeReaderIdentity() {

        Bundle args = getArguments();

        if (args != null) {

            currentStoryId = args.getString(ARG_STORY_ID);

            String title = args.getString(ARG_STORY_TITLE);

            if (title != null && !title.trim().isEmpty()) {
                currentStoryTitle = title.trim();
            }

            currentStoryAuthor = args.getString("storyAuthor");
            currentStorySource = args.getString("storySource");
            currentStorySourceUrl = args.getString("storySourceUrl");
            currentStoryLicense = args.getString("storyLicense");
        }

        if (tvReaderAttribution != null) {

            StringBuilder attribution = new StringBuilder();

            if (currentStoryAuthor != null
                    && !currentStoryAuthor.trim().isEmpty()) {

                attribution.append("Tác giả: ")
                        .append(currentStoryAuthor.trim());
            }

            if (currentStorySource != null
                    && !currentStorySource.trim().isEmpty()) {

                if (attribution.length() > 0) {
                    attribution.append("\n");
                }

                attribution.append("Nguồn: ")
                        .append(currentStorySource.trim());
            }

            if (currentStorySourceUrl != null
                    && !currentStorySourceUrl.trim().isEmpty()) {

                if (attribution.length() > 0) {
                    attribution.append("\n");
                }

                attribution.append(currentStorySourceUrl.trim());
            }

            if (currentStoryLicense != null
                    && !currentStoryLicense.trim().isEmpty()) {

                if (attribution.length() > 0) {
                    attribution.append("\n");
                }

                attribution.append("Giấy phép: ")
                        .append(currentStoryLicense.trim());
            }

            tvReaderAttribution.setText(attribution.toString());

            tvReaderAttribution.setVisibility(
                    attribution.length() == 0
                            ? View.GONE
                            : View.VISIBLE
            );
        }

        if (currentStoryId == null
                || currentStoryId.trim().isEmpty()) {

            currentStoryId = DEFAULT_STORY_ID;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            currentUserId = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

        } else {

            currentUserId = readerPreferences.getString(
                    KEY_GUEST_USER_ID,
                    null
            );

            if (currentUserId == null
                    || currentUserId.trim().isEmpty()) {

                currentUserId = "guest_" + UUID.randomUUID();

                readerPreferences.edit()
                        .putString(KEY_GUEST_USER_ID, currentUserId)
                        .apply();
            }
        }
    }

    private void initViews(View view) {

        btnBack = view.findViewById(R.id.btn_back);
        btnChapterList = view.findViewById(R.id.btn_chapter_list);
        btnReaderSettings = view.findViewById(R.id.btn_reader_settings);

        btnPreviousChapter =
                view.findViewById(R.id.btn_previous_chapter);

        btnNextChapter =
                view.findViewById(R.id.btn_next_chapter);

        tvReaderTitle = view.findViewById(R.id.tv_reader_title);
        tvChapterNumber = view.findViewById(R.id.tv_chapter_number);
        tvChapterTitle = view.findViewById(R.id.tv_chapter_title);
        tvChapterContent = view.findViewById(R.id.tv_chapter_content);
        tvChapterProgress = view.findViewById(R.id.tv_chapter_progress);
        tvReaderAttribution =
                view.findViewById(R.id.tv_reader_attribution);

        progressReading = view.findViewById(R.id.progress_reading);
        scrollReader = view.findViewById(R.id.scroll_reader);
    }

    private void setupButtonAppearance() {

        ColorStateList buttonTint =
                ColorStateList.valueOf(PRIMARY_COLOR);

        btnPreviousChapter.setBackgroundTintList(buttonTint);
        btnNextChapter.setBackgroundTintList(buttonTint);

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

            if (!canGoPrevious) {
                return;
            }

            ChapterEntity currentChapter =
                    readerViewModel.getCurrentChapter().getValue();

            if (currentChapter == null) {
                return;
            }

            saveCurrentReadingState();
            readerViewModel.previousChapter();
        });

        btnNextChapter.setOnClickListener(v -> {

            if (!canGoNext) {
                return;
            }

            ChapterEntity currentChapter =
                    readerViewModel.getCurrentChapter().getValue();

            if (currentChapter == null) {
                return;
            }

            saveCurrentReadingState();
            readerViewModel.nextChapter();
        });

        btnReaderSettings.setOnClickListener(
                v -> showReaderSettings()
        );

        scrollReader.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                    updateReadingProgress();

                    if (!isRestoringScroll) {
                        saveProgressIfNeeded();
                    }
                }
        );
    }

    private void showChapterList() {

        View bottomSheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_chapter_list,
                null
        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        dialog.setContentView(bottomSheetView);

        RecyclerView recyclerView =
                bottomSheetView.findViewById(R.id.rv_chapter_list);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        ChapterAdapter adapter = new ChapterAdapter(chapter -> {

            if (chapter == null) {
                return;
            }

            saveCurrentReadingState();

            readerViewModel.selectChapter(chapter);

            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);

        List<ChapterEntity> chapters =
                readerViewModel.getChapters().getValue();

        adapter.setChapters(chapters);

        TextView tvChapterListSubtitle =
                bottomSheetView.findViewById(
                        R.id.tv_chapter_list_subtitle
                );

        int chapterCount =
                chapters != null ? chapters.size() : 0;

        if (tvChapterListSubtitle != null) {

            tvChapterListSubtitle.setText(
                    chapterCount > 0
                            ? chapterCount
                              + " chương · Chọn chương bạn muốn tiếp tục đọc"
                            : "Truyện chưa có chương được lưu trên thiết bị"
            );
        }

        ChapterEntity currentChapter =
                readerViewModel.getCurrentChapter().getValue();

        if (currentChapter != null) {
            adapter.setCurrentChapter(
                    currentChapter.getChapterId()
            );
        }

        if (chapters == null || chapters.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Chưa có chương nào để hiển thị",
                    Toast.LENGTH_SHORT
            ).show();
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

        View bottomSheetView = getLayoutInflater().inflate(
                R.layout.bottom_sheet_reader_settings,
                null
        );

        BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(requireContext());

        bottomSheetDialog.setContentView(bottomSheetView);

        RadioGroup rgFontSize =
                bottomSheetView.findViewById(R.id.rg_font_size);

        RadioButton rbFontSmall =
                bottomSheetView.findViewById(R.id.rb_font_small);

        RadioButton rbFontMedium =
                bottomSheetView.findViewById(R.id.rb_font_medium);

        RadioButton rbFontLarge =
                bottomSheetView.findViewById(R.id.rb_font_large);

        RadioGroup rgBackground =
                bottomSheetView.findViewById(
                        R.id.rg_reader_background
                );

        RadioButton rbBgLight =
                bottomSheetView.findViewById(R.id.rb_bg_light);

        RadioButton rbBgCream =
                bottomSheetView.findViewById(R.id.rb_bg_cream);

        RadioButton rbBgDark =
                bottomSheetView.findViewById(R.id.rb_bg_dark);

        RadioGroup rgLineSpacing =
                bottomSheetView.findViewById(R.id.rg_line_spacing);

        RadioButton rbSpacingCompact =
                bottomSheetView.findViewById(
                        R.id.rb_spacing_compact
                );

        RadioButton rbSpacingNormal =
                bottomSheetView.findViewById(
                        R.id.rb_spacing_normal
                );

        RadioButton rbSpacingRelaxed =
                bottomSheetView.findViewById(
                        R.id.rb_spacing_relaxed
                );

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

        rgLineSpacing.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.rb_spacing_compact) {
                        currentLineSpacingExtra =
                                LINE_SPACING_COMPACT;

                    } else if (checkedId == R.id.rb_spacing_relaxed) {
                        currentLineSpacingExtra =
                                LINE_SPACING_RELAXED;

                    } else {
                        currentLineSpacingExtra =
                                LINE_SPACING_NORMAL;
                    }

                    applyReaderSettings();
                    saveReaderSettings();
                }
        );

        rgBackground.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.rb_bg_light) {
                        currentBackground = "light";

                    } else if (checkedId == R.id.rb_bg_dark) {
                        currentBackground = "dark";

                    } else {
                        currentBackground = "cream";
                    }

                    applyReaderSettings();
                    saveReaderSettings();
                }
        );

        bottomSheetDialog.show();
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

        if (btnBack != null) {
            btnBack.setColorFilter(iconColor);
        }

        if (btnChapterList != null) {
            btnChapterList.setColorFilter(iconColor);
        }

        if (btnReaderSettings != null) {
            btnReaderSettings.setColorFilter(iconColor);
        }

        View header = requireView().findViewById(R.id.reader_header);

        View bottomBar =
                requireView().findViewById(R.id.reader_bottom_bar);

        if (header != null) {
            header.setBackgroundColor(chromeColor);
        }

        if (bottomBar != null) {
            bottomBar.setBackgroundColor(chromeColor);
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

    private void observeViewModel() {

        readerViewModel.getChapters().observe(
                getViewLifecycleOwner(),
                chapters -> {

                    if (chapters == null || chapters.isEmpty()) {

                        chaptersLoaded = false;

                        updateChapterButtons(null);

                        tvChapterProgress.setText("0 / 0");

                        tvChapterNumber.setText("CHƯA CÓ CHƯƠNG");

                        tvChapterTitle.setText("");

                        tvChapterContent.setText(
                                "Truyện này hiện chưa có nội dung chương "
                                        + "được lưu trong thiết bị. Hãy quay lại "
                                        + "trang thông tin truyện để kiểm tra "
                                        + "nguồn đọc hoặc tải nội dung khi có Internet."
                        );

                        return;
                    }

                    chaptersLoaded = true;

                    updateChapterProgress();

                    if (!progressLoadRequested) {

                        progressLoadRequested = true;

                        readerViewModel.loadProgress(
                                currentUserId,
                                currentStoryId
                        );
                    }

                    if (progressLoaded
                            && readerViewModel.getCurrentChapter()
                            .getValue() == null) {

                        readerViewModel.selectChapter(chapters.get(0));
                    }
                }
        );

        readerViewModel.getCurrentChapter().observe(
                getViewLifecycleOwner(),
                this::displayChapter
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

                    ReadingProgressEntity progress =
                            readerViewModel.getReadingProgress().getValue();

                    List<ChapterEntity> chapterList =
                            readerViewModel.getChapters().getValue();

                    if (chapterList == null || chapterList.isEmpty()) {
                        return;
                    }

                    progressLoaded = true;

                    // Người dùng chưa từng đọc truyện này.
                    if (progress == null) {

                        pendingScrollPosition = 0;
                        shouldRestoreScroll = false;

                        readerViewModel.selectChapter(
                                chapterList.get(0)
                        );

                        return;
                    }

                    // Khôi phục vị trí đọc trước đó.
                    pendingScrollPosition = Math.max(
                            0,
                            progress.getLastPosition()
                    );

                    shouldRestoreScroll = true;

                    // Tìm chương đã đọc gần nhất.
                    ChapterEntity savedChapter = null;

                    for (ChapterEntity chapter : chapterList) {

                        if (chapter != null
                                && chapter.getChapterId() != null
                                && chapter.getChapterId().equals(
                                progress.getChapterId()
                        )) {

                            savedChapter = chapter;
                            break;
                        }
                    }

                    if (savedChapter != null) {

                        readerViewModel.selectChapter(savedChapter);

                    } else {

                        pendingScrollPosition = 0;
                        shouldRestoreScroll = false;

                        readerViewModel.selectChapter(
                                chapterList.get(0)
                        );
                    }
                }
        );

        readerViewModel.getLoading().observe(
                getViewLifecycleOwner(),
                isLoading -> {
                    // Có thể kết nối ProgressBar loading tại đây.
                }
        );

        readerViewModel.getError().observe(
                getViewLifecycleOwner(),
                message -> {

                    if (message != null && !message.isEmpty()) {

                        Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void displayChapter(ChapterEntity chapter) {

        if (chapter == null) {
            return;
        }

        lastSavedProgress = -1;

        tvReaderTitle.setText(currentStoryTitle);

        tvChapterNumber.setText(
                "CHƯƠNG " + chapter.getChapterNumber()
        );

        tvChapterTitle.setText(
                chapter.getTitle() != null
                        ? chapter.getTitle()
                        : ""
        );

        String chapterContent = chapter.getContent();

        if (chapterContent == null
                || chapterContent.trim().isEmpty()) {

            tvChapterContent.setText(
                    "Chương này hiện chỉ có thông tin, chưa có "
                            + "nội dung được lưu. Hãy kiểm tra nguồn truyện "
                            + "hoặc kết nối Internet để tải nội dung hợp pháp."
            );

            applyReaderSettings();
            updateChapterProgress();
            updateChapterButtons(
                    readerViewModel.getChapters().getValue()
            );

            return;
        }

        tvChapterContent.setText(chapterContent);

        applyReaderSettings();
        updateChapterProgress();
        saveHistoryIfNeeded(chapter);

        scrollReader.post(() -> {

            isRestoringScroll = true;

            if (shouldRestoreScroll) {

                if (scrollReader.getChildCount() > 0) {

                    int contentHeight =
                            scrollReader.getChildAt(0).getHeight();

                    int visibleHeight = scrollReader.getHeight();

                    int maxScroll = Math.max(
                            0,
                            contentHeight - visibleHeight
                    );

                    int safePosition = Math.max(
                            0,
                            Math.min(pendingScrollPosition, maxScroll)
                    );

                    scrollReader.scrollTo(0, safePosition);
                }

                shouldRestoreScroll = false;

            } else {

                scrollReader.scrollTo(0, 0);
            }

            updateReadingProgress();

            isRestoringScroll = false;
        });
    }

    private void updateChapterButtons(
            List<ChapterEntity> chapters
    ) {

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

        int currentIndex = findChapterIndex(
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
        canGoNext = currentIndex < chapters.size() - 1;

        setButtonState(btnPreviousChapter, canGoPrevious);
        setButtonState(btnNextChapter, canGoNext);
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

        button.setTextColor(BUTTON_TEXT_COLOR);
        button.setEnabled(enabled);

        button.setAlpha(
                enabled ? 1.0f : DISABLED_ALPHA
        );
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
                    && chapter.getChapterId() != null
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

        if (chapters == null
                || chapters.isEmpty()
                || current == null) {

            updateChapterButtons(chapters);
            return;
        }

        int currentIndex = findChapterIndex(
                chapters,
                current.getChapterId()
        );

        if (currentIndex < 0) {

            updateChapterButtons(chapters);
            return;
        }

        tvChapterProgress.setText(
                (currentIndex + 1) + " / " + chapters.size()
        );

        updateChapterButtons(chapters);
    }

    private int calculateReadingProgress() {

        if (scrollReader.getChildCount() == 0) {
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

        return Math.max(
                0,
                Math.min(100, progress)
        );
    }

    private void updateReadingProgress() {

        if (progressReading == null) {
            return;
        }

        int progress = calculateReadingProgress();

        progressReading.setProgress(progress);
    }

    private void saveProgressIfNeeded() {

        if (isRestoringScroll) {
            return;
        }

        ChapterEntity currentChapter =
                readerViewModel.getCurrentChapter().getValue();

        if (currentChapter == null) {
            return;
        }

        int progress = calculateReadingProgress();

        if (progress == lastSavedProgress) {
            return;
        }

        saveCurrentProgress(
                currentChapter,
                progress
        );
    }

    private void saveCurrentProgress(
            ChapterEntity chapter,
            int progressPercent
    ) {

        if (chapter == null
                || chapter.getChapterId() == null
                || currentUserId == null
                || currentStoryId == null) {

            return;
        }

        int lastPosition = scrollReader.getScrollY();

        ReadingProgressEntity progress =
                new ReadingProgressEntity();

        progress.setProgressId(
                currentUserId + "_" + currentStoryId
        );

        progress.setUserId(currentUserId);
        progress.setStoryId(currentStoryId);
        progress.setChapterId(chapter.getChapterId());
        progress.setProgressPercent(progressPercent);
        progress.setLastPosition(lastPosition);
        progress.setLastReadAt(System.currentTimeMillis());

        lastSavedProgress = progressPercent;

        readerViewModel.saveProgress(progress);
    }

    private void saveCurrentReadingState() {

        if (readerViewModel == null) {
            return;
        }

        ChapterEntity currentChapter =
                readerViewModel.getCurrentChapter().getValue();

        if (currentChapter == null) {
            return;
        }

        int progress = calculateReadingProgress();

        saveCurrentProgress(
                currentChapter,
                progress
        );
    }

    private void saveHistoryIfNeeded(
            ChapterEntity chapter
    ) {

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

        history.setHistoryId(
                UUID.randomUUID().toString()
        );

        history.setUserId(currentUserId);
        history.setStoryId(currentStoryId);
        history.setChapterId(chapterId);
        history.setReadAt(System.currentTimeMillis());

        readerViewModel.saveHistory(history);
    }

    @Override
    public void onPause() {

        if (readerViewModel != null) {
            saveCurrentReadingState();
        }

        super.onPause();
    }
}
