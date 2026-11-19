package com.mobilityshield.api.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FcmService {

    private static final Logger log = LoggerFactory.getLogger(FcmService.class);

    private final FirebaseApp firebaseApp;

    public FcmService(FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
    }

    public boolean sendPushNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("Skip FCM notification: No FCM token provided");
            return false;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance(firebaseApp).send(messageBuilder.build());
            log.info("FCM push notification sent successfully: {}", response);
            return true;
        } catch (Exception e) {
            // Non-fatal: Keep notification delivery failures from breaking business transactions
            log.warn("FCM push delivery failed for token [{}...]: {}",
                    fcmToken.length() > 8 ? fcmToken.substring(0, 8) : fcmToken, e.getMessage());
            return false;
        }
    }
}
