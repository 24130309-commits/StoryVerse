package com.example.storyverse.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storyverse.R;
import com.example.storyverse.adapter.LibraryStoryAdapter;
import com.example.storyverse.data.local.entity.StoryEntity;
import com.example.storyverse.ui.story.StoryDetailFragment;
import com.example.storyverse.viewmodel.LibraryViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LibraryFragment extends Fragment {

    private RecyclerView rvLibrary;
    private TextView tvEmptyLibrary, tvLibraryCount;
    private LibraryStoryAdapter adapter;
    private LibraryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLibrary = view.findViewById(R.id.rvLibrary);
        tvEmptyLibrary = view.findViewById(R.id.tvEmptyLibrary);
        tvLibraryCount = view.findViewById(R.id.tvLibraryCount);

        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        adapter = new LibraryStoryAdapter(new LibraryStoryAdapter.OnLibraryItemListener() {
            @Override
            public void onStoryClick(StoryEntity story) {
                StoryDetailFragment detailFragment = StoryDetailFragment.newInstance(story.getStoryId());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(((ViewGroup) requireView().getParent()).getId(), detailFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onRemoveClick(StoryEntity story) {
                viewModel.removeFavorite(getCurrentUserId(), story.getStoryId());
                Toast.makeText(getContext(), "Đã xóa khỏi thư viện", Toast.LENGTH_SHORT).show();
            }
        });

        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLibrary.setAdapter(adapter);

        viewModel.getFavoriteStories().observe(getViewLifecycleOwner(), stories -> {
            adapter.setStories(stories);
            int count = stories != null ? stories.size() : 0;
            tvLibraryCount.setText(count + " truyện đã lưu");
            tvEmptyLibrary.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadFavorites(getCurrentUserId());
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "guest_user";
    }
}