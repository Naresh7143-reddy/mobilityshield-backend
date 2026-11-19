package com.mobilityshield.api.audit;

import com.google.cloud.firestore.annotation.DocumentId;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AuditLog implements Serializable {

    @DocumentId
    private String logId;
    private String adminUid;
    private String action;
    private String targetType; // DEVICE, USER, POLICY
    private String targetId;
    private String description;
    private String timestamp;
    private Map<String, Object> metadata = new HashMap<>();

    public AuditLog() {
        this.timestamp = Instant.now().toString();
    }

    public AuditLog(String logId, String adminUid, String action, String targetType,
                    String targetId, String description, Map<String, Object> metadata) {
        this.logId = logId;
        this.adminUid = adminUid;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
        this.timestamp = Instant.now().toString();
        if (metadata != null) {
            this.metadata = metadata;
        }
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
