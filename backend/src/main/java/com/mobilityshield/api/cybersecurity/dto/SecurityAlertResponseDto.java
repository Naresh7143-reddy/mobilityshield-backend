package com.mobilityshield.api.cybersecurity.dto;

import com.mobilityshield.api.cybersecurity.SecurityAlert;

public class SecurityAlertResponseDto {

    private String alertId;
    private String userId;
    private String deviceId;
    private String title;
    private String description;
    private String severity;
    private String category;
    private Boolean read;
    private String status;
    private String createdAt;

    public SecurityAlertResponseDto() {}

    public SecurityAlertResponseDto(SecurityAlert alert) {
        if (alert != null) {
            this.alertId = alert.getAlertId();
            this.userId = alert.getUserId();
            this.deviceId = alert.getDeviceId();
            this.title = alert.getTitle();
            this.description = alert.getDescription();
            this.severity = alert.getSeverity();
            this.category = alert.getCategory();
            this.read = alert.getRead();
            this.status = alert.getStatus();
            this.createdAt = alert.getCreatedAt();
        }
    }

    public static SecurityAlertResponseDto from(SecurityAlert alert) {
        return new SecurityAlertResponseDto(alert);
    }

    public String getAlertId() {
        return alertId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getRead() {
        return read;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
