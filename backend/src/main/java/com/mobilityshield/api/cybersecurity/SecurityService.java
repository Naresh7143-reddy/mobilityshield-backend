package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.cybersecurity.dto.RecordSecurityEventRequest;
import com.mobilityshield.api.cybersecurity.dto.SecurityAlertResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityComplianceResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityEventResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityScoreResponseDto;
import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.exception.ResourceNotFoundException;
import com.mobilityshield.api.policy.DevicePolicy;
import com.mobilityshield.api.policy.PolicyRepository;
import com.mobilityshield.api.security.SecurityContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    private final SecurityEventRepository eventRepository;
    private final SecurityAlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final PolicyRepository policyRepository;
    private final SecurityScoreService scoreService;
    private final ThreatDetectionService threatDetectionService;
    private final LoginAttemptRepository loginAttemptRepository;
    private final SecurityContextService securityContextService;

    public SecurityService(SecurityEventRepository eventRepository,
                           SecurityAlertRepository alertRepository,
                           DeviceRepository deviceRepository,
                           PolicyRepository policyRepository,
                           SecurityScoreService scoreService,
                           ThreatDetectionService threatDetectionService,
                           LoginAttemptRepository loginAttemptRepository,
                           SecurityContextService securityContextService) {
        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
        this.deviceRepository = deviceRepository;
        this.policyRepository = policyRepository;
        this.scoreService = scoreService;
        this.threatDetectionService = threatDetectionService;
        this.loginAttemptRepository = loginAttemptRepository;
        this.securityContextService = securityContextService;
    }

    public SecurityScoreResponseDto getSecurityScore(String deviceId) {
        Device device = getDeviceAndCheckAccess(deviceId);
        return scoreService.calculateSecurityScore(device);
    }

    public SecurityEventResponseDto recordSecurityEvent(RecordSecurityEventRequest request) {
        Device device = getDeviceAndCheckAccess(request.getDeviceId());

        String eventId = "evt-" + UUID.randomUUID().toString();
        String now = Instant.now().toString();

        String severity = request.getSeverity();
        if (severity == null || severity.isBlank()) {
            severity = classifySeverity(request.getEventType());
        }

        SecurityEvent event = new SecurityEvent();
        event.setEventId(eventId);
        event.setDeviceId(device.getDeviceId());
        event.setUserId(device.getUserId());
        event.setEventType(request.getEventType().toUpperCase());
        event.setDescription(request.getDescription());
        event.setSeverity(severity);
        event.setTimestamp(now);
        event.setSource(request.getSource() != null ? request.getSource() : "Android Agent");
        if (request.getMetadata() != null) {
            event.setMetadata(request.getMetadata());
        }

        SecurityEvent saved = eventRepository.save(event);

        // Automatically create alert for elevated threats
        if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            threatDetectionService.createAlert(device.getUserId(), device.getDeviceId(),
                    "Security Threat Detected: " + request.getEventType(),
                    request.getDescription(), severity, "THREAT_DETECTION");
        }

        // Check if root detected should trigger quarantine based on policy
        DevicePolicy policy = policyRepository.getEffectivePolicy();
        if ("ROOT_DETECTED".equalsIgnoreCase(request.getEventType()) && Boolean.TRUE.equals(policy.getBlockRootedDevices())) {
            threatDetectionService.triggerDeviceQuarantine(device,
                    "Security Policy Violation: Rooting / superuser binary detected on mobile device.");
        }

        // Recalculate security score dynamically
        scoreService.calculateSecurityScore(device);

        return SecurityEventResponseDto.from(saved);
    }

    public List<SecurityEventResponseDto> getSecurityEvents(String deviceId, int limit) {
        if (deviceId != null && !deviceId.isBlank()) {
            getDeviceAndCheckAccess(deviceId);
            return eventRepository.findByDeviceId(deviceId, limit).stream()
                    .map(SecurityEventResponseDto::from)
                    .collect(Collectors.toList());
        }

        if (securityContextService.isAdmin()) {
            return eventRepository.findAll(limit).stream()
                    .map(SecurityEventResponseDto::from)
                    .collect(Collectors.toList());
        }

        String currentUid = securityContextService.getCurrentUid();
        return eventRepository.findByUserId(currentUid, limit).stream()
                .map(SecurityEventResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<SecurityAlertResponseDto> getSecurityAlerts(String deviceId) {
        if (deviceId != null && !deviceId.isBlank()) {
            getDeviceAndCheckAccess(deviceId);
            return alertRepository.findByDeviceId(deviceId).stream()
                    .map(SecurityAlertResponseDto::from)
                    .collect(Collectors.toList());
        }

        if (securityContextService.isAdmin()) {
            return alertRepository.findAll().stream()
                    .map(SecurityAlertResponseDto::from)
                    .collect(Collectors.toList());
        }

        String currentUid = securityContextService.getCurrentUid();
        return alertRepository.findByUserId(currentUid).stream()
                .map(SecurityAlertResponseDto::from)
                .collect(Collectors.toList());
    }

    public SecurityAlertResponseDto getSecurityAlertById(String alertId) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Security alert not found with ID: " + alertId));
        securityContextService.assertOwnershipOrAdmin(alert.getUserId());
        return SecurityAlertResponseDto.from(alert);
    }

    public void markAlertAsRead(String alertId) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Security alert not found with ID: " + alertId));
        securityContextService.assertOwnershipOrAdmin(alert.getUserId());
        alertRepository.markAsRead(alertId);
    }

    public SecurityComplianceResponseDto getDeviceCompliance(String deviceId) {
        Device device = getDeviceAndCheckAccess(deviceId);
        DevicePolicy policy = policyRepository.getEffectivePolicy();

        List<SecurityEvent> events = eventRepository.findByDeviceId(deviceId, 30);
        boolean rootDetected = false;
        boolean screenLockDisabled = false;

        for (SecurityEvent ev : events) {
            String type = ev.getEventType() != null ? ev.getEventType().toUpperCase() : "";
            if ("ROOT_DETECTED".equals(type) || "ROOT_PRESENT".equals(type)) {
                rootDetected = true;
            } else if ("SCREEN_LOCK_DISABLED".equals(type)) {
                screenLockDisabled = true;
            }
        }

        List<String> violations = new ArrayList<>();
        if (rootDetected && Boolean.TRUE.equals(policy.getBlockRootedDevices())) {
            violations.add("Root detected on device (Policy prohibits rooted devices)");
        }
        if (screenLockDisabled && Boolean.TRUE.equals(policy.getRequireScreenLock())) {
            violations.add("Screen lock is disabled (Policy requires active screen lock)");
        }

        boolean osCompliant = true;
        try {
            double devVer = Double.parseDouble(device.getAndroidVersion().replaceAll("[^0-9.]", ""));
            double minVer = Double.parseDouble(policy.getMinimumAndroidVersion().replaceAll("[^0-9.]", ""));
            if (devVer < minVer) {
                osCompliant = false;
                violations.add("Android version " + device.getAndroidVersion() + " is below minimum allowed " + policy.getMinimumAndroidVersion());
            }
        } catch (Exception ignored) {}

        int failedLogins = loginAttemptRepository.countConsecutiveFailuresForDevice(deviceId);
        if (failedLogins >= (policy.getMaximumFailedAttempts() != null ? policy.getMaximumFailedAttempts() : 3)) {
            violations.add("Consecutive failed application logins exceed limit (" + failedLogins + ")");
        }

        boolean overallCompliant = violations.isEmpty() && !Boolean.TRUE.equals(device.getQuarantineStatus());

        return new SecurityComplianceResponseDto(deviceId, overallCompliant, rootDetected,
                !screenLockDisabled, device.getAndroidVersion(), policy.getMinimumAndroidVersion(),
                osCompliant, failedLogins, device.getQuarantineStatus(), violations, Instant.now().toString());
    }

    private String classifySeverity(String eventType) {
        if (eventType == null) return "LOW";
        return switch (eventType.toUpperCase()) {
            case "ROOT_DETECTED", "MALWARE_DETECTED" -> "CRITICAL";
            case "SCREEN_LOCK_DISABLED", "FAILED_LOGIN" -> "HIGH";
            case "UNSUPPORTED_OS", "POLICY_VIOLATION" -> "MEDIUM";
            default -> "LOW";
        };
    }

    private Device getDeviceAndCheckAccess(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));
        securityContextService.assertOwnershipOrAdmin(device.getUserId());
        return device;
    }
}
