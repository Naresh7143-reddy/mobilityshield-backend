package com.mobilityshield.api.admin;

import com.mobilityshield.api.admin.dto.AdminDashboardDto;
import com.mobilityshield.api.admin.dto.AuditLogResponseDto;
import com.mobilityshield.api.admin.dto.QuarantineDeviceRequest;
import com.mobilityshield.api.admin.dto.RecoverDeviceRequest;
import com.mobilityshield.api.admin.dto.UpdateDeviceStatusRequest;
import com.mobilityshield.api.admin.dto.UpdateUserStatusRequest;
import com.mobilityshield.api.audit.AuditService;
import com.mobilityshield.api.cybersecurity.SecurityAlert;
import com.mobilityshield.api.cybersecurity.SecurityAlertRepository;
import com.mobilityshield.api.cybersecurity.SecurityEvent;
import com.mobilityshield.api.cybersecurity.SecurityEventRepository;
import com.mobilityshield.api.cybersecurity.SecurityScoreService;
import com.mobilityshield.api.cybersecurity.dto.SecurityAlertResponseDto;
import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.exception.BadRequestException;
import com.mobilityshield.api.exception.ResourceNotFoundException;
import com.mobilityshield.api.notification.NotificationService;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.user.User;
import com.mobilityshield.api.user.UserRepository;
import com.mobilityshield.api.user.dto.UserResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final SecurityAlertRepository alertRepository;
    private final SecurityEventRepository eventRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SecurityContextService securityContextService;
    private final SecurityScoreService scoreService;

    public AdminService(UserRepository userRepository,
                        DeviceRepository deviceRepository,
                        SecurityAlertRepository alertRepository,
                        SecurityEventRepository eventRepository,
                        AuditService auditService,
                        NotificationService notificationService,
                        SecurityContextService securityContextService,
                        SecurityScoreService scoreService) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.securityContextService = securityContextService;
        this.scoreService = scoreService;
    }

    public AdminDashboardDto getDashboardStats() {
        List<Device> devices = deviceRepository.findAll();
        List<User> users = userRepository.findAll();
        List<SecurityAlert> alerts = alertRepository.findAll();

        long totalDevices = devices.size();
        long activeDevices = devices.stream().filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus())).count();
        long quarantinedDevices = devices.stream().filter(d -> Boolean.TRUE.equals(d.getQuarantineStatus())).count();
        long inactiveDevices = devices.stream().filter(d -> "INACTIVE".equalsIgnoreCase(d.getStatus())).count();

        long totalUsers = users.size();
        long activeUsers = users.stream().filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus())).count();

        long openAlerts = alerts.stream().filter(a -> "OPEN".equalsIgnoreCase(a.getStatus())).count();
        long criticalAlerts = alerts.stream().filter(a -> "CRITICAL".equalsIgnoreCase(a.getSeverity())).count();

        double avgScore = devices.isEmpty() ? 100.0 : devices.stream()
                .mapToInt(d -> d.getSecurityScore() != null ? d.getSecurityScore() : 100)
                .average().orElse(100.0);

        Map<String, Long> riskCounts = new HashMap<>();
        riskCounts.put("LOW", devices.stream().filter(d -> (d.getSecurityScore() != null ? d.getSecurityScore() : 100) >= 80).count());
        riskCounts.put("MEDIUM", devices.stream().filter(d -> {
            int s = d.getSecurityScore() != null ? d.getSecurityScore() : 100;
            return s >= 60 && s < 80;
        }).count());
        riskCounts.put("HIGH", devices.stream().filter(d -> {
            int s = d.getSecurityScore() != null ? d.getSecurityScore() : 100;
            return s >= 40 && s < 60;
        }).count());
        riskCounts.put("CRITICAL", devices.stream().filter(d -> (d.getSecurityScore() != null ? d.getSecurityScore() : 100) < 40).count());

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalDevices(totalDevices);
        dto.setActiveDevices(activeDevices);
        dto.setQuarantinedDevices(quarantinedDevices);
        dto.setInactiveDevices(inactiveDevices);
        dto.setTotalUsers(totalUsers);
        dto.setActiveUsers(activeUsers);
        dto.setOpenAlertsCount(openAlerts);
        dto.setCriticalAlertsCount(criticalAlerts);
        dto.setAverageSecurityScore(Math.round(avgScore * 10.0) / 10.0);
        dto.setDevicesByRisk(riskCounts);

        return dto;
    }

    public List<UserResponseDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<DeviceResponseDto> getDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<SecurityAlertResponseDto> getAlerts() {
        return alertRepository.findAll().stream()
                .map(SecurityAlertResponseDto::from)
                .collect(Collectors.toList());
    }

    public UserResponseDto updateUserStatus(String uid, UpdateUserStatusRequest request) {
        String adminUid = securityContextService.getCurrentUid();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UID: " + uid));

        String oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        user.setUpdatedAt(Instant.now().toString());
        User updated = userRepository.save(user);

        // Audit Log
        Map<String, Object> meta = new HashMap<>();
        meta.put("oldStatus", oldStatus);
        meta.put("newStatus", request.getStatus());
        meta.put("reason", request.getReason());
        auditService.recordAction(adminUid, "UPDATE_USER_STATUS", "USER", uid,
                "Admin changed user status from [" + oldStatus + "] to [" + request.getStatus() + "]", meta);

        log.info("Admin [{}] updated status for user [{}] to [{}]", adminUid, uid, request.getStatus());
        return UserResponseDto.from(updated);
    }

    public DeviceResponseDto quarantineDevice(String deviceId, QuarantineDeviceRequest request) {
        String adminUid = securityContextService.getCurrentUid();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        deviceRepository.updateStatusAndQuarantine(deviceId, "QUARANTINED", true, request.getReason());
        device.setStatus("QUARANTINED");
        device.setQuarantineStatus(true);
        device.setQuarantineReason(request.getReason());

        // Re-evaluate score with quarantine penalty
        scoreService.calculateSecurityScore(device);

        // Security event
        SecurityEvent event = new SecurityEvent();
        event.setEventId("evt-" + UUID.randomUUID().toString());
        event.setDeviceId(deviceId);
        event.setUserId(device.getUserId());
        event.setEventType("DEVICE_QUARANTINED");
        event.setDescription("Device quarantined by admin: " + request.getReason());
        event.setSeverity("HIGH");
        event.setSource("Admin Console (" + adminUid + ")");
        eventRepository.save(event);

        // Audit log
        Map<String, Object> meta = new HashMap<>();
        meta.put("reason", request.getReason());
        auditService.recordAction(adminUid, "QUARANTINE_DEVICE", "DEVICE", deviceId,
                "Admin placed device [" + deviceId + "] in quarantine. Reason: " + request.getReason(), meta);

        // Notification
        notificationService.notifyDeviceQuarantined(device.getUserId(), deviceId, request.getReason());

        return DeviceResponseDto.from(device);
    }

    public DeviceResponseDto recoverDevice(String deviceId, RecoverDeviceRequest request) {
        String adminUid = securityContextService.getCurrentUid();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        if (!Boolean.TRUE.equals(device.getQuarantineStatus())) {
            throw new BadRequestException("Device [" + deviceId + "] is not currently quarantined");
        }

        String targetStatus = request.getTargetStatus() != null ? request.getTargetStatus() : "ACTIVE";
        deviceRepository.updateStatusAndQuarantine(deviceId, targetStatus, false, null);
        device.setStatus(targetStatus);
        device.setQuarantineStatus(false);
        device.setQuarantineReason(null);

        // Recalculate security score without quarantine penalty
        scoreService.calculateSecurityScore(device);

        // Security event
        SecurityEvent event = new SecurityEvent();
        event.setEventId("evt-" + UUID.randomUUID().toString());
        event.setDeviceId(deviceId);
        event.setUserId(device.getUserId());
        event.setEventType("DEVICE_RECOVERED");
        event.setDescription("Device recovered from quarantine by admin. Justification: " + request.getReason());
        event.setSeverity("LOW");
        event.setSource("Admin Console (" + adminUid + ")");
        eventRepository.save(event);

        // Audit log
        Map<String, Object> meta = new HashMap<>();
        meta.put("recoveryReason", request.getReason());
        meta.put("restoredStatus", targetStatus);
        auditService.recordAction(adminUid, "RECOVER_DEVICE", "DEVICE", deviceId,
                "Admin lifted quarantine for device [" + deviceId + "]. Justification: " + request.getReason(), meta);

        // Notification
        notificationService.notifyDeviceRecovered(device.getUserId(), deviceId, request.getReason());

        log.info("Admin [{}] recovered device [{}] from quarantine", adminUid, deviceId);
        return DeviceResponseDto.from(device);
    }

    public DeviceResponseDto updateDeviceStatus(String deviceId, UpdateDeviceStatusRequest request) {
        String adminUid = securityContextService.getCurrentUid();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        String oldStatus = device.getStatus();
        deviceRepository.updateStatusAndQuarantine(deviceId, request.getStatus(),
                device.getQuarantineStatus(), device.getQuarantineReason());
        device.setStatus(request.getStatus());

        Map<String, Object> meta = new HashMap<>();
        meta.put("oldStatus", oldStatus);
        meta.put("newStatus", request.getStatus());
        meta.put("reason", request.getReason());
        auditService.recordAction(adminUid, "UPDATE_DEVICE_STATUS", "DEVICE", deviceId,
                "Admin updated device status from [" + oldStatus + "] to [" + request.getStatus() + "]", meta);

        return DeviceResponseDto.from(device);
    }

    public List<AuditLogResponseDto> getAuditLogs(int limit) {
        return auditService.getAuditLogs(limit).stream()
                .map(AuditLogResponseDto::from)
                .collect(Collectors.toList());
    }
}
