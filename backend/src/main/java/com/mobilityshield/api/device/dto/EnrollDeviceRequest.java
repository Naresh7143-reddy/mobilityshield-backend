package com.mobilityshield.api.device.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnrollDeviceRequest {

    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must not exceed 100 characters")
    private String deviceName;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Android version is required")
    private String androidVersion;

    @NotNull(message = "SDK version is required")
    @Min(value = 1, message = "SDK version must be positive")
    private Integer sdkVersion;

    private String securityPatchLevel;

    @Min(value = 0, message = "Battery percentage cannot be less than 0")
    @Max(value = 100, message = "Battery percentage cannot exceed 100")
    private Integer batteryPercentage;

    private Boolean charging;
    private Long totalRam;
    private Long availableRam;
    private Long totalStorage;
    private Long availableStorage;
    private String networkType;
    private Boolean wifiConnected;

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
}
