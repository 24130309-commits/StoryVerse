package com.example.storyverse.data.remote.firebase;

import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseMessagingManager {

    private final FirebaseMessaging messaging;

    public FirebaseMessagingManager() {
        messaging = FirebaseMessaging.getInstance();
    }

    public FirebaseMessaging getMessaging() {
        return messaging;
    }

    public void getDeviceToken(
            FirebaseMessagingManager.TokenCallback callback
    ) {
        messaging.getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(
                                task.getException()
                        );
                        return;
                    }

                    callback.onSuccess(
                            task.getResult()
                    );
                });
    }

    public interface TokenCallback {

        void onSuccess(String token);

        void onFailure(Exception exception);
    }
}