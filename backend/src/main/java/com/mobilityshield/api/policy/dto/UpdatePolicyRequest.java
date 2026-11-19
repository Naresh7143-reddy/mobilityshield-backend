package com.mobilityshield.api.policy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdatePolicyRequest {

    private String policyId;

    @NotBlank(message = "Policy name is required")
    private String policyName;

    private String description;

    @NotBlank(message = "Minimum Android version is required")
    private String minimumAndroidVersion;

    @NotNull(message = "Screen lock requirement must be specified")
    private Boolean requireScreenLock;

    @NotNull(message = "Root blocking requirement must be specified")
    private Boolean blockRootedDevices;

    @NotNull(message = "Maximum failed attempts must be specified")
    @Min(value = 1, message = "Maximum failed attempts must be at least 1")
    private Integer maximumFailedAttempts;

    private Boolean enabled = true;

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMinimumAndroidVersion() {
        return minimumAndroidVersion;
    }

    public void setMinimumAndroidVersion(String minimumAndroidVersion) {
        this.minimumAndroidVersion = minimumAndroidVersion;
    }

    public Boolean getRequireScreenLock() {
        return requireScreenLock;
    }

    public void setRequireScreenLock(Boolean requireScreenLock) {
        this.requireScreenLock = requireScreenLock;
    }

    public Boolean getBlockRootedDevices() {
        return blockRootedDevices;
    }

    public void setBlockRootedDevices(Boolean blockRootedDevices) {
        this.blockRootedDevices = blockRootedDevices;
    }

    public Integer getMaximumFailedAttempts() {
        return maximumFailedAttempts;
    }

    public void setMaximumFailedAttempts(Integer maximumFailedAttempts) {
        this.maximumFailedAttempts = maximumFailedAttempts;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
