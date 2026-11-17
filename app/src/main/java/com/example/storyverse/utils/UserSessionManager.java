
package com.example.storyverse.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public final class UserSessionManager {

    private static final String PREF_NAME = "reader_settings";
    private static final String KEY_GUEST_USER_ID = "guest_user_id";

    private UserSessionManager() {
    }

    public static String getCurrentUserId(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance()
                .getCurrentUser();

        if (user != null) {
            return user.getUid();
        }

        SharedPreferences preferences = context
                .getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String guestUserId = preferences.getString(
                KEY_GUEST_USER_ID,
                null
        );

        if (guestUserId == null || guestUserId.trim().isEmpty()) {
            guestUserId = "guest_" + UUID.randomUUID();

            preferences.edit()
                    .putString(KEY_GUEST_USER_ID, guestUserId)
                    .apply();
        }

        return guestUserId;
    }
}
