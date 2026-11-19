package com.mobilityshield.api.admin.dto;

import java.util.Map;

public class AdminDashboardDto {

    private long totalDevices;
    private long activeDevices;
    private long quarantinedDevices;
    private long inactiveDevices;
    private long totalUsers;
    private long activeUsers;
    private long openAlertsCount;
    private long criticalAlertsCount;
    private double averageSecurityScore;
    private Map<String, Long> devicesByRisk;

    public AdminDashboardDto() {}

    public long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public long getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(long activeDevices) {
        this.activeDevices = activeDevices;
    }

    public long getQuarantinedDevices() {
        return quarantinedDevices;
    }

    public void setQuarantinedDevices(long quarantinedDevices) {
        this.quarantinedDevices = quarantinedDevices;
    }

    public long getInactiveDevices() {
        return inactiveDevices;
    }

    public void setInactiveDevices(long inactiveDevices) {
        this.inactiveDevices = inactiveDevices;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getOpenAlertsCount() {
        return openAlertsCount;
    }

    public void setOpenAlertsCount(long openAlertsCount) {
        this.openAlertsCount = openAlertsCount;
    }

    public long getCriticalAlertsCount() {
        return criticalAlertsCount;
    }

    public void setCriticalAlertsCount(long criticalAlertsCount) {
        this.criticalAlertsCount = criticalAlertsCount;
    }

    public double getAverageSecurityScore() {
        return averageSecurityScore;
    }

    public void setAverageSecurityScore(double averageSecurityScore) {
        this.averageSecurityScore = averageSecurityScore;
    }

    public Map<String, Long> getDevicesByRisk() {
        return devicesByRisk;
    }

    public void setDevicesByRisk(Map<String, Long> devicesByRisk) {
        this.devicesByRisk = devicesByRisk;
    }
}
