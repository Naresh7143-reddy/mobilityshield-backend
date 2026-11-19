package com.mobilityshield.api.cybersecurity.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class RecordSecurityEventRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Event type is required")
    private String eventType; // ROOT_DETECTED, SCREEN_LOCK_DISABLED, SUSPICIOUS_ACTIVITY, etc.

    @NotBlank(message = "Description is required")
    private String description;

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String source;

    private Map<String, Object> metadata;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
