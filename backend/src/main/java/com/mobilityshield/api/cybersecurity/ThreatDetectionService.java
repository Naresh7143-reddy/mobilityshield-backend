package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ThreatDetectionService {

    private static final Logger log = LoggerFactory.getLogger(ThreatDetectionService.class);

    private final SecurityAlertRepository alertRepository;
    private final SecurityEventRepository eventRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationService notificationService;

    public ThreatDetectionService(SecurityAlertRepository alertRepository,
                                  SecurityEventRepository eventRepository,
                                  DeviceRepository deviceRepository,
                                  NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.eventRepository = eventRepository;
        this.deviceRepository = deviceRepository;
        this.notificationService = notificationService;
    }

    public SecurityAlert createAlert(String userId, String deviceId, String title,
                                     String description, String severity, String category) {
        String alertId = "alt-" + UUID.randomUUID().toString();
        SecurityAlert alert = new SecurityAlert();
        alert.setAlertId(alertId);
        alert.setUserId(userId);
        alert.setDeviceId(deviceId);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setCategory(category);
        alert.setRead(false);
        alert.setStatus("OPEN");
        alert.setCreatedAt(Instant.now().toString());

        SecurityAlert saved = alertRepository.save(alert);

        if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            notificationService.notifyAdmins("Critical Alert: " + title, description, null);
        }

        return saved;
    }

    public void triggerDeviceQuarantine(Device device, String reason) {
        if (Boolean.TRUE.equals(device.getQuarantineStatus()) && "QUARANTINED".equalsIgnoreCase(device.getStatus())) {
            log.debug("Device [{}] is already quarantined", device.getDeviceId());
            return;
        }

        log.warn("Initiating quarantine for device [{}] (Owner: [{}]). Reason: {}",
                device.getDeviceId(), device.getUserId(), reason);

        deviceRepository.updateStatusAndQuarantine(device.getDeviceId(), "QUARANTINED", true, reason);

        // Record security alert
        createAlert(device.getUserId(), device.getDeviceId(),
                "Device Quarantined Automatically",
                reason, "CRITICAL", "DEVICE_QUARANTINE");

        // Record security event
        SecurityEvent event = new SecurityEvent();
        event.setEventId("evt-" + UUID.randomUUID().toString());
        event.setUserId(device.getUserId());
        event.setDeviceId(device.getDeviceId());
        event.setEventType("DEVICE_QUARANTINED");
        event.setDescription("Device placed in quarantine: " + reason);
        event.setSeverity("CRITICAL");
        event.setSource("MobilityShield Security Engine");
        event.getMetadata().put("reason", reason);
        eventRepository.save(event);

        // Notify user and admins
        notificationService.notifyDeviceQuarantined(device.getUserId(), device.getDeviceId(), reason);
    }
}
