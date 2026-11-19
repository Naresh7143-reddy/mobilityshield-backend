package com.mobilityshield.api.admin.dto;

import com.mobilityshield.api.audit.AuditLog;

import java.util.Map;

public class AuditLogResponseDto {

    private String logId;
    private String adminUid;
    private String action;
    private String targetType;
    private String targetId;
    private String description;
    private String timestamp;
    private Map<String, Object> metadata;

    public AuditLogResponseDto() {}

    public AuditLogResponseDto(AuditLog log) {
        if (log != null) {
            this.logId = log.getLogId();
            this.adminUid = log.getAdminUid();
            this.action = log.getAction();
            this.targetType = log.getTargetType();
            this.targetId = log.getTargetId();
            this.description = log.getDescription();
            this.timestamp = log.getTimestamp();
            this.metadata = log.getMetadata();
        }
    }

    public static AuditLogResponseDto from(AuditLog log) {
        return new AuditLogResponseDto(log);
    }

    public String getLogId() {
        return logId;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
