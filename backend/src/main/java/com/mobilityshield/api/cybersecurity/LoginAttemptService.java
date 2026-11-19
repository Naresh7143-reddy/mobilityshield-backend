package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.common.Constants;
import com.mobilityshield.api.cybersecurity.dto.RecordLoginAttemptRequest;
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
import java.util.UUID;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final LoginAttemptRepository loginAttemptRepository;
    private final DeviceRepository deviceRepository;
    private final PolicyRepository policyRepository;
    private final ThreatDetectionService threatDetectionService;
    private final SecurityContextService securityContextService;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository,
                               DeviceRepository deviceRepository,
                               PolicyRepository policyRepository,
                               ThreatDetectionService threatDetectionService,
                               SecurityContextService securityContextService) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.deviceRepository = deviceRepository;
        this.policyRepository = policyRepository;
        this.threatDetectionService = threatDetectionService;
        this.securityContextService = securityContextService;
    }

    public LoginAttempt recordLoginAttempt(RecordLoginAttemptRequest request) {
        String deviceId = request.getDeviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        String currentUid = securityContextService.getCurrentUid();
        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        String attemptId = "att-" + UUID.randomUUID().toString();
        String now = Instant.now().toString();

        LoginAttempt attempt = new LoginAttempt();
        attempt.setAttemptId(attemptId);
        attempt.setDeviceId(deviceId);
        attempt.setUserId(device.getUserId());
        attempt.setSuccess(request.getSuccess());
        attempt.setTimestamp(now);
        attempt.setSource(request.getSource() != null ? request.getSource() : "MobilityShield Client App");
        attempt.setRiskLevel(Boolean.TRUE.equals(request.getSuccess()) ? "NONE" : "ELEVATED");

        LoginAttempt savedAttempt = loginAttemptRepository.save(attempt);

        if (Boolean.FALSE.equals(request.getSuccess())) {
            int consecutiveFailures = loginAttemptRepository.countConsecutiveFailuresForDevice(deviceId);
            log.warn("Detected failed login attempt on device [{}]. Consecutive failures count: {}",
                    deviceId, consecutiveFailures);

            DevicePolicy policy = policyRepository.getEffectivePolicy();
            int maxAllowedFailures = policy.getMaximumFailedAttempts() != null
                    ? policy.getMaximumFailedAttempts()
                    : Constants.MAX_FAILED_LOGIN_ATTEMPTS;

            if (consecutiveFailures >= maxAllowedFailures) {
                String quarantineReason = "Security Policy Violation: Exceeded maximum allowed consecutive failed application login attempts ("
                        + consecutiveFailures + "/" + maxAllowedFailures + "). Device quarantined automatically.";
                log.warn("Triggering 3-attempt quarantine rule for device [{}]", deviceId);
                threatDetectionService.triggerDeviceQuarantine(device, quarantineReason);
            }
        }

        return savedAttempt;
    }

    public int getConsecutiveFailures(String deviceId) {
        return loginAttemptRepository.countConsecutiveFailuresForDevice(deviceId);
    }
}
