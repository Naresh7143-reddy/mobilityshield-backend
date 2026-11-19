package com.mobilityshield.api.cybersecurity.dto;

import java.util.List;

public class SecurityComplianceResponseDto {

    private String deviceId;
    private Boolean compliant;
    private Boolean rootDetected;
    private Boolean screenLockEnabled;
    private String androidVersion;
    private String minimumAndroidVersion;
    private Boolean osCompliant;
    private Integer failedLoginAttempts;
    private Boolean quarantined;
    private List<String> violations;
    private String evaluatedAt;

    public SecurityComplianceResponseDto() {}

    public SecurityComplianceResponseDto(String deviceId, Boolean compliant, Boolean rootDetected,
                                         Boolean screenLockEnabled, String androidVersion,
                                         String minimumAndroidVersion, Boolean osCompliant,
                                         Integer failedLoginAttempts, Boolean quarantined,
                                         List<String> violations, String evaluatedAt) {
        this.deviceId = deviceId;
        this.compliant = compliant;
        this.rootDetected = rootDetected;
        this.screenLockEnabled = screenLockEnabled;
        this.androidVersion = androidVersion;
        this.minimumAndroidVersion = minimumAndroidVersion;
        this.osCompliant = osCompliant;
        this.failedLoginAttempts = failedLoginAttempts;
        this.quarantined = quarantined;
        this.violations = violations;
        this.evaluatedAt = evaluatedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Boolean getCompliant() {
        return compliant;
    }

    public Boolean getRootDetected() {
        return rootDetected;
    }

    public Boolean getScreenLockEnabled() {
        return screenLockEnabled;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getMinimumAndroidVersion() {
        return minimumAndroidVersion;
    }

    public Boolean getOsCompliant() {
        return osCompliant;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Boolean getQuarantined() {
        return quarantined;
    }

    public List<String> getViolations() {
        return violations;
    }

    public String getEvaluatedAt() {
        return evaluatedAt;
    }
}
