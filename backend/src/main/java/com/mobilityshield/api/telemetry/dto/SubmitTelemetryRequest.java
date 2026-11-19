package com.mobilityshield.api.telemetry.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitTelemetryRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Battery percentage is required")
    @Min(value = 0, message = "Battery percentage must be at least 0")
    @Max(value = 100, message = "Battery percentage cannot exceed 100")
    private Integer batteryPercentage;

    private Boolean charging;

    @Min(value = 0, message = "Available RAM must be positive")
    private Long availableRam;

    @Min(value = 0, message = "Total RAM must be positive")
    private Long totalRam;

    @Min(value = 0, message = "Available storage must be positive")
    private Long availableStorage;

    @Min(value = 0, message = "Total storage must be positive")
    private Long totalStorage;

    private String networkType;
    private Boolean wifiConnected;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
}
