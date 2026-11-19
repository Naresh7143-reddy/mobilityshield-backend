package com.mobilityshield.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecoverDeviceRequest {

    @NotBlank(message = "Recovery justification reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    private String targetStatus = "ACTIVE";

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}
