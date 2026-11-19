package com.mobilityshield.api.cybersecurity.dto;

import java.util.List;
import java.util.Map;

public class SecurityScoreResponseDto {

    private String deviceId;
    private Integer securityScore;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private List<Map<String, Object>> activeDeductions;
    private String calculatedAt;

    public SecurityScoreResponseDto() {}

    public SecurityScoreResponseDto(String deviceId, Integer securityScore, String riskLevel,
                                    List<Map<String, Object>> activeDeductions, String calculatedAt) {
        this.deviceId = deviceId;
        this.securityScore = securityScore;
        this.riskLevel = riskLevel;
        this.activeDeductions = activeDeductions;
        this.calculatedAt = calculatedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getSecurityScore() {
        return securityScore;
    }

    public void setSecurityScore(Integer securityScore) {
        this.securityScore = securityScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<Map<String, Object>> getActiveDeductions() {
        return activeDeductions;
    }

    public void setActiveDeductions(List<Map<String, Object>> activeDeductions) {
        this.activeDeductions = activeDeductions;
    }

    public String getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(String calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
