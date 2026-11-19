package com.mobilityshield.api.cybersecurity;

import com.google.cloud.firestore.annotation.DocumentId;

import java.io.Serializable;
import java.time.Instant;

public class LoginAttempt implements Serializable {

    @DocumentId
    private String attemptId;
    private String userId;
    private String deviceId;
    private Boolean success;
    private String timestamp;
    private String source;
    private String riskLevel;

    public LoginAttempt() {
        this.timestamp = Instant.now().toString();
    }

    public String getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
