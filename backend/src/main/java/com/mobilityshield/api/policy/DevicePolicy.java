package com.mobilityshield.api.policy;

import com.google.cloud.firestore.annotation.DocumentId;

import java.io.Serializable;
import java.time.Instant;

public class DevicePolicy implements Serializable {

    @DocumentId
    private String policyId;
    private String policyName;
    private String description;
    private String minimumAndroidVersion = "12.0";
    private Boolean requireScreenLock = true;
    private Boolean blockRootedDevices = true;
    private Integer maximumFailedAttempts = 3;
    private Boolean enabled = true;
    private String updatedAt;
    private String updatedBy;

    public DevicePolicy() {
        this.updatedAt = Instant.now().toString();
    }

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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
