package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.cybersecurity.dto.RecordLoginAttemptRequest;
import com.mobilityshield.api.cybersecurity.dto.SecurityScoreResponseDto;
import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.notification.NotificationService;
import com.mobilityshield.api.policy.DevicePolicy;
import com.mobilityshield.api.policy.PolicyRepository;
import com.mobilityshield.api.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CybersecurityTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private SecurityAlertRepository alertRepository;

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private ThreatDetectionService threatDetectionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityContextService securityContextService;

    private SecurityScoreService scoreService;
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        scoreService = new SecurityScoreService(deviceRepository, policyRepository, eventRepository);
        loginAttemptService = new LoginAttemptService(loginAttemptRepository, deviceRepository,
                policyRepository, threatDetectionService, securityContextService);
    }

    @Test
    @DisplayName("Calculate security score deducts points for root and outdated OS without duplicate deduction")
    void testSecurityScoreCalculationAndDeduplication() {
        DevicePolicy policy = new DevicePolicy();
        policy.setMinimumAndroidVersion("14.0");
        when(policyRepository.getEffectivePolicy()).thenReturn(policy);

        Device device = new Device();
        device.setDeviceId("dev-score-1");
        device.setAndroidVersion("11.0"); // Outdated (-15)
        device.setQuarantineStatus(false);

        // Multiple root detected events should only deduct once (-30)
        SecurityEvent ev1 = new SecurityEvent();
        ev1.setEventType("ROOT_DETECTED");
        SecurityEvent ev2 = new SecurityEvent();
        ev2.setEventType("ROOT_DETECTED");

        when(eventRepository.findByDeviceId("dev-score-1", 50)).thenReturn(Arrays.asList(ev1, ev2));

        SecurityScoreResponseDto response = scoreService.calculateSecurityScore(device);

        assertNotNull(response);
        // Initial 100 - 30 (root) - 15 (outdated OS) = 55
        assertEquals(55, response.getSecurityScore());
        assertEquals("HIGH", response.getRiskLevel()); // 40-59 is HIGH risk
        assertEquals(2, response.getActiveDeductions().size(), "Must avoid duplicate deductions for same condition");
    }

    @Test
    @DisplayName("Three consecutive failed logins triggers device quarantine")
    void testThreeFailedLoginsTriggersQuarantine() {
        Device device = new Device();
        device.setDeviceId("dev-login-3");
        device.setUserId("user-bob");
        device.setStatus("ACTIVE");
        device.setQuarantineStatus(false);

        DevicePolicy policy = new DevicePolicy();
        policy.setMaximumFailedAttempts(3);

        when(deviceRepository.findById("dev-login-3")).thenReturn(Optional.of(device));
        when(policyRepository.getEffectivePolicy()).thenReturn(policy);
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        // 3 consecutive failures returned
        when(loginAttemptRepository.countConsecutiveFailuresForDevice("dev-login-3")).thenReturn(3);

        RecordLoginAttemptRequest request = new RecordLoginAttemptRequest();
        request.setDeviceId("dev-login-3");
        request.setSuccess(false);

        loginAttemptService.recordLoginAttempt(request);

        // Verify quarantine trigger was invoked
        verify(threatDetectionService, times(1)).triggerDeviceQuarantine(eq(device), contains("Exceeded maximum allowed consecutive failed"));
    }

    @Test
    @DisplayName("Successful login does not trigger quarantine")
    void testSuccessfulLoginDoesNotTriggerQuarantine() {
        Device device = new Device();
        device.setDeviceId("dev-login-ok");
        device.setUserId("user-bob");
        device.setStatus("ACTIVE");
        device.setQuarantineStatus(false);

        when(deviceRepository.findById("dev-login-ok")).thenReturn(Optional.of(device));
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenAnswer(i -> i.getArgument(0));

        RecordLoginAttemptRequest request = new RecordLoginAttemptRequest();
        request.setDeviceId("dev-login-ok");
        request.setSuccess(true);

        loginAttemptService.recordLoginAttempt(request);

        verify(threatDetectionService, never()).triggerDeviceQuarantine(any(), any());
    }
}
