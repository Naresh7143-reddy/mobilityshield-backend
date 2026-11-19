package com.mobilityshield.api.admin;

import com.mobilityshield.api.admin.dto.AdminDashboardDto;
import com.mobilityshield.api.admin.dto.QuarantineDeviceRequest;
import com.mobilityshield.api.admin.dto.RecoverDeviceRequest;
import com.mobilityshield.api.audit.AuditService;
import com.mobilityshield.api.cybersecurity.SecurityAlertRepository;
import com.mobilityshield.api.cybersecurity.SecurityEventRepository;
import com.mobilityshield.api.cybersecurity.SecurityScoreService;
import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.exception.BadRequestException;
import com.mobilityshield.api.notification.NotificationService;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCloudManagementTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private SecurityAlertRepository alertRepository;

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private SecurityScoreService scoreService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, deviceRepository, alertRepository,
                eventRepository, auditService, notificationService, securityContextService, scoreService);
    }

    @Test
    @DisplayName("Admin dashboard calculates aggregate statistics correctly")
    void testDashboardStatistics() {
        Device d1 = new Device();
        d1.setStatus("ACTIVE");
        d1.setQuarantineStatus(false);
        d1.setSecurityScore(95);

        Device d2 = new Device();
        d2.setStatus("QUARANTINED");
        d2.setQuarantineStatus(true);
        d2.setSecurityScore(45);

        when(deviceRepository.findAll()).thenReturn(java.util.Arrays.asList(d1, d2));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(alertRepository.findAll()).thenReturn(Collections.emptyList());

        AdminDashboardDto stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(2, stats.getTotalDevices());
        assertEquals(1, stats.getActiveDevices());
        assertEquals(1, stats.getQuarantinedDevices());
        assertEquals(70.0, stats.getAverageSecurityScore());
    }

    @Test
    @DisplayName("Quarantine updates device, creates audit log, and notifies user")
    void testAdminQuarantine() {
        when(securityContextService.getCurrentUid()).thenReturn("admin-007");

        Device device = new Device();
        device.setDeviceId("dev-q1");
        device.setUserId("user-owner");
        device.setStatus("ACTIVE");
        device.setQuarantineStatus(false);

        when(deviceRepository.findById("dev-q1")).thenReturn(Optional.of(device));

        QuarantineDeviceRequest request = new QuarantineDeviceRequest();
        request.setReason("Malware activity confirmed by analyst");

        DeviceResponseDto response = adminService.quarantineDevice("dev-q1", request);

        assertNotNull(response);
        assertEquals("QUARANTINED", response.getStatus());
        assertTrue(response.getQuarantineStatus());
        verify(deviceRepository).updateStatusAndQuarantine("dev-q1", "QUARANTINED", true, "Malware activity confirmed by analyst");
        verify(auditService).recordAction(eq("admin-007"), eq("QUARANTINE_DEVICE"), eq("DEVICE"), eq("dev-q1"), anyString(), any());
        verify(notificationService).notifyDeviceQuarantined("user-owner", "dev-q1", "Malware activity confirmed by analyst");
    }

    @Test
    @DisplayName("Recovery clears quarantine flags, records audit log, and notifies user")
    void testAdminRecovery() {
        when(securityContextService.getCurrentUid()).thenReturn("admin-007");

        Device device = new Device();
        device.setDeviceId("dev-r1");
        device.setUserId("user-owner");
        device.setStatus("QUARANTINED");
        device.setQuarantineStatus(true);
        device.setQuarantineReason("Prior breach");

        when(deviceRepository.findById("dev-r1")).thenReturn(Optional.of(device));

        RecoverDeviceRequest request = new RecoverDeviceRequest();
        request.setReason("Remediation verified by cybersecurity team");

        DeviceResponseDto response = adminService.recoverDevice("dev-r1", request);

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        assertFalse(response.getQuarantineStatus());
        verify(deviceRepository).updateStatusAndQuarantine("dev-r1", "ACTIVE", false, null);
        verify(auditService).recordAction(eq("admin-007"), eq("RECOVER_DEVICE"), eq("DEVICE"), eq("dev-r1"), anyString(), any());
        verify(notificationService).notifyDeviceRecovered("user-owner", "dev-r1", "Remediation verified by cybersecurity team");
    }

    @Test
    @DisplayName("Recovery on non-quarantined device throws BadRequestException")
    void testRecoverNonQuarantinedDevice() {
        when(securityContextService.getCurrentUid()).thenReturn("admin-007");

        Device device = new Device();
        device.setDeviceId("dev-ok");
        device.setStatus("ACTIVE");
        device.setQuarantineStatus(false);

        when(deviceRepository.findById("dev-ok")).thenReturn(Optional.of(device));

        RecoverDeviceRequest request = new RecoverDeviceRequest();
        request.setReason("Test");

        assertThrows(BadRequestException.class, () -> adminService.recoverDevice("dev-ok", request));
    }
}
