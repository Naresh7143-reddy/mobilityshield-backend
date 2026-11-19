package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.common.Constants;
import com.mobilityshield.api.cybersecurity.dto.SecurityScoreResponseDto;
import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.policy.DevicePolicy;
import com.mobilityshield.api.policy.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SecurityScoreService {

    private static final Logger log = LoggerFactory.getLogger(SecurityScoreService.class);

    private final DeviceRepository deviceRepository;
    private final PolicyRepository policyRepository;
    private final SecurityEventRepository eventRepository;

    public SecurityScoreService(DeviceRepository deviceRepository,
                                PolicyRepository policyRepository,
                                SecurityEventRepository eventRepository) {
        this.deviceRepository = deviceRepository;
        this.policyRepository = policyRepository;
        this.eventRepository = eventRepository;
    }

    public SecurityScoreResponseDto calculateSecurityScore(Device device) {
        DevicePolicy policy = policyRepository.getEffectivePolicy();
        int score = Constants.MAX_SECURITY_SCORE; // Initial 100
        List<Map<String, Object>> activeDeductions = new ArrayList<>();

        // Fetch recent security events for this device to check active conditions without duplicate deductions
        List<SecurityEvent> recentEvents = eventRepository.findByDeviceId(device.getDeviceId(), 50);

        boolean rootActive = false;
        boolean screenLockDisabledActive = false;
        boolean suspiciousActivityActive = false;

        for (SecurityEvent event : recentEvents) {
            String type = event.getEventType() != null ? event.getEventType().toUpperCase() : "";
            if ("ROOT_DETECTED".equals(type) || "ROOT_PRESENT".equals(type)) {
                rootActive = true;
            } else if ("SCREEN_LOCK_DISABLED".equals(type) || "INSECURE_LOCK".equals(type)) {
                screenLockDisabledActive = true;
            } else if ("SUSPICIOUS_ACTIVITY".equals(type) || "SUSPICIOUS_BEHAVIOR".equals(type)) {
                suspiciousActivityActive = true;
            }
        }

        // 1. Root detected check (-30)
        if (rootActive) {
            score -= Constants.DEDUCTION_ROOT_DETECTED;
            Map<String, Object> deduction = new HashMap<>();
            deduction.put("condition", "ROOT_DETECTED");
            deduction.put("pointsDeducted", Constants.DEDUCTION_ROOT_DETECTED);
            deduction.put("reason", "Device integrity compromised: Root access signals detected");
            activeDeductions.add(deduction);
        }

        // 2. Screen lock disabled check (-20)
        if (screenLockDisabledActive) {
            score -= Constants.DEDUCTION_SCREEN_LOCK_DISABLED;
            Map<String, Object> deduction = new HashMap<>();
            deduction.put("condition", "SCREEN_LOCK_DISABLED");
            deduction.put("pointsDeducted", Constants.DEDUCTION_SCREEN_LOCK_DISABLED);
            deduction.put("reason", "Device protection disabled: Screen lock PIN/password/biometric missing");
            activeDeductions.add(deduction);
        }

        // 3. Android version compliance check (-15)
        if (isOsVersionUnsupported(device.getAndroidVersion(), policy.getMinimumAndroidVersion())) {
            score -= Constants.DEDUCTION_UNSUPPORTED_OS;
            Map<String, Object> deduction = new HashMap<>();
            deduction.put("condition", "UNSUPPORTED_ANDROID_VERSION");
            deduction.put("pointsDeducted", Constants.DEDUCTION_UNSUPPORTED_OS);
            deduction.put("reason", "OS outdated: Running Android " + device.getAndroidVersion()
                    + " below enterprise policy baseline " + policy.getMinimumAndroidVersion());
            activeDeductions.add(deduction);
        }

        // 4. Suspicious activity check (-20)
        if (suspiciousActivityActive) {
            score -= Constants.DEDUCTION_SUSPICIOUS_ACTIVITY;
            Map<String, Object> deduction = new HashMap<>();
            deduction.put("condition", "SUSPICIOUS_ACTIVITY");
            deduction.put("pointsDeducted", Constants.DEDUCTION_SUSPICIOUS_ACTIVITY);
            deduction.put("reason", "Anomalous security behavior reported on device");
            activeDeductions.add(deduction);
        }

        // 5. Quarantine state penalty
        if (Boolean.TRUE.equals(device.getQuarantineStatus())) {
            score -= Constants.DEDUCTION_POLICY_VIOLATION;
            Map<String, Object> deduction = new HashMap<>();
            deduction.put("condition", "DEVICE_QUARANTINED");
            deduction.put("pointsDeducted", Constants.DEDUCTION_POLICY_VIOLATION);
            deduction.put("reason", "Device is currently quarantined due to policy violation");
            activeDeductions.add(deduction);
        }

        // Boundaries: 0 <= score <= 100
        score = Math.max(Constants.MIN_SECURITY_SCORE, Math.min(Constants.MAX_SECURITY_SCORE, score));

        // Update score in device entity if changed
        if (!Integer.valueOf(score).equals(device.getSecurityScore())) {
            device.setSecurityScore(score);
            deviceRepository.updateSecurityScore(device.getDeviceId(), score);
        }

        String riskLevel = determineRiskLevel(score);
        return new SecurityScoreResponseDto(device.getDeviceId(), score, riskLevel,
                activeDeductions, Instant.now().toString());
    }

    public String determineRiskLevel(int score) {
        if (score >= 80) return "LOW";
        if (score >= 60) return "MEDIUM";
        if (score >= 40) return "HIGH";
        return "CRITICAL";
    }

    private boolean isOsVersionUnsupported(String deviceVersion, String minVersion) {
        if (deviceVersion == null || minVersion == null) return false;
        try {
            double dVer = parseVersionPrefix(deviceVersion);
            double mVer = parseVersionPrefix(minVersion);
            return dVer < mVer;
        } catch (Exception e) {
            return false;
        }
    }

    private double parseVersionPrefix(String version) {
        String cleaned = version.replaceAll("[^0-9.]", "").trim();
        String[] parts = cleaned.split("\\.");
        if (parts.length == 0 || parts[0].isEmpty()) return 0.0;
        if (parts.length == 1) return Double.parseDouble(parts[0]);
        return Double.parseDouble(parts[0] + "." + parts[1]);
    }
}
