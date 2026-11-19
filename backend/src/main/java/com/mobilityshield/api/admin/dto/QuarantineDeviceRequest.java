package com.mobilityshield.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuarantineDeviceRequest {

    @NotBlank(message = "Quarantine reason is required")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
