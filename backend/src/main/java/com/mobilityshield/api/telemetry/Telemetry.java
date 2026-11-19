package com.mobilityshield.api.telemetry;

import com.google.cloud.firestore.annotation.DocumentId;

import java.io.Serializable;
import java.time.Instant;

public class Telemetry implements Serializable {

    @DocumentId
    private String telemetryId;
    private String deviceId;
    private String userId;
    private Integer batteryPercentage;
    private Boolean charging;
    private Long availableRam;
    private Long totalRam;
    private Long availableStorage;
    private Long totalStorage;
    private String networkType;
    private Boolean wifiConnected;
    private String timestamp;

    public Telemetry() {
        this.timestamp = Instant.now().toString();
    }

    public String getTelemetryId() {
        return telemetryId;
    }

    public void setTelemetryId(String telemetryId) {
        this.telemetryId = telemetryId;
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

    public Long getAvailableRam() {
        return availableRam;
    }

    public void setAvailableRam(Long availableRam) {
        this.availableRam = availableRam;
    }

    public Long getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(Long totalRam) {
        this.totalRam = totalRam;
    }

    public Long getAvailableStorage() {
        return availableStorage;
    }

    public void setAvailableStorage(Long availableStorage) {
        this.availableStorage = availableStorage;
    }

    public Long getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(Long totalStorage) {
        this.totalStorage = totalStorage;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
