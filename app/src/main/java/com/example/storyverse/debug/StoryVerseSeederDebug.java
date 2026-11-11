
package com.example.storyverse.debug;

import android.util.Log;

import com.example.storyverse.data.remote.firebase.StoryVerseMockDataSeeder;

public final class StoryVerseSeederDebug {

    private static final String TAG = "StoryVerseSeederDebug";

    private StoryVerseSeederDebug() {
    }

    public interface ResultCallback {
        void onResult(boolean success, String message);
    }

    public static void run(ResultCallback callback) {

        StoryVerseMockDataSeeder.seed360((success, message) -> {

            if (success) {
                Log.d(TAG, message);
            } else {
                Log.e(TAG, message);
            }

            if (callback != null) {
                callback.onResult(success, message);
            }
        });
    }
}
