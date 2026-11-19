package com.mobilityshield.api.notification;

import com.mobilityshield.api.security.Role;
import com.mobilityshield.api.user.User;
import com.mobilityshield.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final FcmService fcmService;
    private final UserRepository userRepository;

    public NotificationService(FcmService fcmService, UserRepository userRepository) {
        this.fcmService = fcmService;
        this.userRepository = userRepository;
    }

    public void notifyUser(String userId, String title, String body, Map<String, String> data) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) {
                fcmService.sendPushNotification(user.getFcmToken(), title, body, data);
            }
        });
    }

    public void notifyAdmins(String title, String body, Map<String, String> data) {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (Role.ADMIN.equals(u.getRole()) && u.getFcmToken() != null && !u.getFcmToken().isBlank()) {
                fcmService.sendPushNotification(u.getFcmToken(), title, body, data);
            }
        }
    }

    public void notifyDeviceQuarantined(String userId, String deviceId, String reason) {
        Map<String, String> data = new HashMap<>();
        data.put("deviceId", deviceId);
        data.put("action", "QUARANTINE");
        data.put("reason", reason);

        notifyUser(userId, "Device Quarantined",
                "Your device [" + deviceId + "] was placed in quarantine: " + reason, data);
        notifyAdmins("Alert: Device Quarantined",
                "Device [" + deviceId + "] of user [" + userId + "] quarantined: " + reason, data);
    }

    public void notifyDeviceRecovered(String userId, String deviceId, String reason) {
        Map<String, String> data = new HashMap<>();
        data.put("deviceId", deviceId);
        data.put("action", "RECOVER");
        data.put("reason", reason);

        notifyUser(userId, "Device Quarantine Lifted",
                "Your device [" + deviceId + "] has been recovered by an administrator: " + reason, data);
    }
}
