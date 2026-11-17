
package com.example.storyverse;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.storyverse.data.remote.firebase.RealtimeCatalogSyncManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private RealtimeCatalogSyncManager syncManager;

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private View navHost;
    private ConstraintLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        syncManager = new RealtimeCatalogSyncManager(
                getApplicationContext()
        );

        rootLayout = findViewById(R.id.main);
        navHost = findViewById(R.id.nav_host_fragment);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupNavigation();
        setupWindowInsets();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            return;
        }

        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(
                bottomNavigationView,
                navController
        );

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) ->
                        updateBottomNavigation(destination)
        );

        updateBottomNavigation(navController.getCurrentDestination());
    }

    private void updateBottomNavigation(NavDestination destination) {
        if (destination == null) {
            return;
        }

        int destinationId = destination.getId();

        boolean showBottomNavigation =
                destinationId == R.id.homeFragment2
                        || destinationId == R.id.exploreFragment
                        || destinationId == R.id.libraryFragment
                        || destinationId == R.id.profileFragment;

        bottomNavigationView.setVisibility(
                showBottomNavigation ? View.VISIBLE : View.GONE
        );

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        constraintSet.clear(
                R.id.nav_host_fragment,
                ConstraintSet.BOTTOM
        );

        if (showBottomNavigation) {
            constraintSet.connect(
                    R.id.nav_host_fragment,
                    ConstraintSet.BOTTOM,
                    R.id.bottom_navigation,
                    ConstraintSet.TOP
            );
        } else {
            constraintSet.connect(
                    R.id.nav_host_fragment,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
            );
        }

        constraintSet.applyTo(rootLayout);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                rootLayout,
                (view, windowInsets) -> {

                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    navHost.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            0
                    );

                    bottomNavigationView.setPadding(
                            bottomNavigationView.getPaddingLeft(),
                            bottomNavigationView.getPaddingTop(),
                            bottomNavigationView.getPaddingRight(),
                            systemBars.bottom
                    );

                    return windowInsets;
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (syncManager != null) {
            syncManager.start();
        }
    }

    @Override
    protected void onStop() {
        if (syncManager != null) {
            syncManager.stop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (syncManager != null) {
            syncManager.shutdown();
            syncManager = null;
        }

        super.onDestroy();
    }
}
