package com.mobilityshield.api.device.dto;

import java.util.List;

public class DeviceHealthResponseDto {

    private String deviceId;
    private String healthStatus;      // EXCELLENT, GOOD, WARNING, CRITICAL
    private String batteryStatus;     // HEALTHY, LOW, CRITICAL
    private String ramStatus;         // HEALTHY, HIGH_USAGE, CRITICAL
    private String storageStatus;     // HEALTHY, LOW_SPACE, CRITICAL
    private String connectivityStatus;// ONLINE, OFFLINE
    private String lastSeen;
    private List<String> issues;

    public DeviceHealthResponseDto() {}

    public DeviceHealthResponseDto(String deviceId, String healthStatus, String batteryStatus,
                                   String ramStatus, String storageStatus, String connectivityStatus,
                                   String lastSeen, List<String> issues) {
        this.deviceId = deviceId;
        this.healthStatus = healthStatus;
        this.batteryStatus = batteryStatus;
        this.ramStatus = ramStatus;
        this.storageStatus = storageStatus;
        this.connectivityStatus = connectivityStatus;
        this.lastSeen = lastSeen;
        this.issues = issues;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getRamStatus() {
        return ramStatus;
    }

    public void setRamStatus(String ramStatus) {
        this.ramStatus = ramStatus;
    }

    public String getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(String storageStatus) {
        this.storageStatus = storageStatus;
    }

    public String getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(String connectivityStatus) {
        this.connectivityStatus = connectivityStatus;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
}
