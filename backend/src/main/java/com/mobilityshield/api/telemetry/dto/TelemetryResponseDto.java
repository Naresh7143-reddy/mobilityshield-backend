package com.mobilityshield.api.telemetry.dto;

import com.mobilityshield.api.telemetry.Telemetry;

public class TelemetryResponseDto {

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

    public TelemetryResponseDto() {}

    public TelemetryResponseDto(Telemetry telemetry) {
        if (telemetry != null) {
            this.telemetryId = telemetry.getTelemetryId();
            this.deviceId = telemetry.getDeviceId();
            this.userId = telemetry.getUserId();
            this.batteryPercentage = telemetry.getBatteryPercentage();
            this.charging = telemetry.getCharging();
            this.availableRam = telemetry.getAvailableRam();
            this.totalRam = telemetry.getTotalRam();
            this.availableStorage = telemetry.getAvailableStorage();
            this.totalStorage = telemetry.getTotalStorage();
            this.networkType = telemetry.getNetworkType();
            this.wifiConnected = telemetry.getWifiConnected();
            this.timestamp = telemetry.getTimestamp();
        }
    }

    public static TelemetryResponseDto from(Telemetry telemetry) {
        return new TelemetryResponseDto(telemetry);
    }

    public String getTelemetryId() {
        return telemetryId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getBatteryPercentage() {
        return batteryPercentage;
    }

    public Boolean getCharging() {
        return charging;
    }

    public Long getAvailableRam() {
        return availableRam;
    }

    public Long getTotalRam() {
        return totalRam;
    }

    public Long getAvailableStorage() {
        return availableStorage;
    }

    public Long getTotalStorage() {
        return totalStorage;
    }

    public String getNetworkType() {
        return networkType;
    }

    public Boolean getWifiConnected() {
        return wifiConnected;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
