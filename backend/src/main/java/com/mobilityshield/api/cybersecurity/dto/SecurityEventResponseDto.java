package com.mobilityshield.api.cybersecurity.dto;

import com.mobilityshield.api.cybersecurity.SecurityEvent;

import java.util.Map;

public class SecurityEventResponseDto {

    private String eventId;
    private String userId;
    private String deviceId;
    private String eventType;
    private String description;
    private String severity;
    private String timestamp;
    private String source;
    private Map<String, Object> metadata;

    public SecurityEventResponseDto() {}

    public SecurityEventResponseDto(SecurityEvent event) {
        if (event != null) {
            this.eventId = event.getEventId();
            this.userId = event.getUserId();
            this.deviceId = event.getDeviceId();
            this.eventType = event.getEventType();
            this.description = event.getDescription();
            this.severity = event.getSeverity();
            this.timestamp = event.getTimestamp();
            this.source = event.getSource();
            this.metadata = event.getMetadata();
        }
    }

    public static SecurityEventResponseDto from(SecurityEvent event) {
        return new SecurityEventResponseDto(event);
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
