package com.mobilityshield.api.device.dto;

import com.mobilityshield.api.device.Device;

public class DeviceResponseDto {

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
    private String status;
    private Integer securityScore;
    private Boolean quarantineStatus;
    private String quarantineReason;

    public DeviceResponseDto() {}

    public DeviceResponseDto(Device device) {
        if (device != null) {
            this.deviceId = device.getDeviceId();
            this.userId = device.getUserId();
            this.deviceName = device.getDeviceName();
            this.manufacturer = device.getManufacturer();
            this.model = device.getModel();
            this.androidVersion = device.getAndroidVersion();
            this.sdkVersion = device.getSdkVersion();
            this.securityPatchLevel = device.getSecurityPatchLevel();
            this.batteryPercentage = device.getBatteryPercentage();
            this.charging = device.getCharging();
            this.totalRam = device.getTotalRam();
            this.availableRam = device.getAvailableRam();
            this.totalStorage = device.getTotalStorage();
            this.availableStorage = device.getAvailableStorage();
            this.networkType = device.getNetworkType();
            this.wifiConnected = device.getWifiConnected();
            this.enrolledAt = device.getEnrolledAt();
            this.lastSeen = device.getLastSeen();
            this.status = device.getStatus();
            this.securityScore = device.getSecurityScore();
            this.quarantineStatus = device.getQuarantineStatus();
            this.quarantineReason = device.getQuarantineReason();
        }
    }

    public static DeviceResponseDto from(Device device) {
        return new DeviceResponseDto(device);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public Integer getSdkVersion() {
        return sdkVersion;
    }

    public String getSecurityPatchLevel() {
        return securityPatchLevel;
    }

    public Integer getBatteryPercentage() {
        return batteryPercentage;
    }

    public Boolean getCharging() {
        return charging;
    }

    public Long getTotalRam() {
        return totalRam;
    }

    public Long getAvailableRam() {
        return availableRam;
    }

    public Long getTotalStorage() {
        return totalStorage;
    }

    public Long getAvailableStorage() {
        return availableStorage;
    }

    public String getNetworkType() {
        return networkType;
    }

    public Boolean getWifiConnected() {
        return wifiConnected;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public Integer getSecurityScore() {
        return securityScore;
    }

    public Boolean getQuarantineStatus() {
        return quarantineStatus;
    }

    public String getQuarantineReason() {
        return quarantineReason;
    }
}
