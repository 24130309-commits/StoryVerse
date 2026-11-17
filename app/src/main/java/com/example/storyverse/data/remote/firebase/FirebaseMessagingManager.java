
package com.example.storyverse.data.remote.firebase;

import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseMessagingManager {

    private final FirebaseMessaging messaging;

    public FirebaseMessagingManager() {
        messaging = FirebaseMessaging.getInstance();
    }

    // Callback dùng chung cho các thao tác FCM
    public interface TokenCallback {
        void onSuccess(String token);
        void onFailure(Exception exception);
    }

    public interface MessagingCallback {
        void onSuccess();
        void onFailure(Exception exception);
    }

    // Lấy FirebaseMessaging
    public FirebaseMessaging getMessaging() {
        return messaging;
    }

    // 1. Lấy FCM Token của thiết bị
    public void getDeviceToken(TokenCallback callback) {

        if (callback == null) {
            return;
        }

        messaging.getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Exception error = task.getException();

                        callback.onFailure(
                                error != null
                                        ? error
                                        : new Exception("Không thể lấy FCM Token")
                        );
                        return;
                    }

                    String token = task.getResult();

                    if (token == null || token.trim().isEmpty()) {
                        callback.onFailure(
                                new Exception("FCM Token không hợp lệ")
                        );
                        return;
                    }

                    callback.onSuccess(token);
                });
    }

    // 2. Đăng ký nhận thông báo theo Topic
    public void subscribeToTopic(
            String topic,
            MessagingCallback callback
    ) {

        if (topic == null || topic.trim().isEmpty()) {
            notifyFailure(
                    callback,
                    new IllegalArgumentException("Topic không hợp lệ")
            );
            return;
        }

        messaging.subscribeToTopic(topic.trim())
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        notifyFailure(
                                callback,
                                getTaskException(task.getException())
                        );
                    }
                });
    }

    // 3. Hủy đăng ký Topic
    public void unsubscribeFromTopic(
            String topic,
            MessagingCallback callback
    ) {

        if (topic == null || topic.trim().isEmpty()) {
            notifyFailure(
                    callback,
                    new IllegalArgumentException("Topic không hợp lệ")
            );
            return;
        }

        messaging.unsubscribeFromTopic(topic.trim())
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        notifyFailure(
                                callback,
                                getTaskException(task.getException())
                        );
                    }
                });
    }

    // Xử lý lỗi
    private void notifyFailure(
            MessagingCallback callback,
            Exception exception
    ) {
        if (callback != null) {
            callback.onFailure(exception);
        }
    }

    private Exception getTaskException(Exception exception) {
        return exception != null
                ? exception
                : new Exception("Thao tác Firebase Messaging thất bại");
    }
}
