package com.mobilityshield.api.device;

import com.google.cloud.firestore.annotation.DocumentId;

import java.io.Serializable;
import java.time.Instant;

public class Device implements Serializable {

    @DocumentId
    private String deviceId;
    private String userId;
    private String deviceName;
    private String manufacturer;
    private String model;
    private String androidVersion;
    private Integer sdkVersion;
    private String securityPatchLevel;
    private Integer batteryPercentage;
    private Boolean charging;
    private Long totalRam;
    private Long availableRam;
    private Long totalStorage;
    private Long availableStorage;
    private String networkType;
    private Boolean wifiConnected;
    private String enrolledAt;
    private String lastSeen;
    private String status = "ACTIVE";
    private Integer securityScore = 100;
    private Boolean quarantineStatus = false;
    private String quarantineReason;

    public Device() {
        String now = Instant.now().toString();
        this.enrolledAt = now;
        this.lastSeen = now;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public Integer getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(Integer sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getSecurityPatchLevel() {
        return securityPatchLevel;
    }

    public void setSecurityPatchLevel(String securityPatchLevel) {
        this.securityPatchLevel = securityPatchLevel;
    }

    public Integer getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(Integer batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public Boolean getCharging() {
        return charging;
    }

    public void setCharging(Boolean charging) {
        this.charging = charging;
    }

    public Long getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(Long totalRam) {
        this.totalRam = totalRam;
    }

    public Long getAvailableRam() {
        return availableRam;
    }

    public void setAvailableRam(Long availableRam) {
        this.availableRam = availableRam;
    }

    public Long getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(Long totalStorage) {
        this.totalStorage = totalStorage;
    }

    public Long getAvailableStorage() {
        return availableStorage;
    }

    public void setAvailableStorage(Long availableStorage) {
        this.availableStorage = availableStorage;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Boolean getWifiConnected() {
        return wifiConnected;
    }

    public void setWifiConnected(Boolean wifiConnected) {
        this.wifiConnected = wifiConnected;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(Integer securityScore) {
        this.securityScore = securityScore;
    }

    public Boolean getQuarantineStatus() {
        return quarantineStatus;
    }

    public void setQuarantineStatus(Boolean quarantineStatus) {
        this.quarantineStatus = quarantineStatus;
    }

    public String getQuarantineReason() {
        return quarantineReason;
    }

    public void setQuarantineReason(String quarantineReason) {
        this.quarantineReason = quarantineReason;
    }
}
