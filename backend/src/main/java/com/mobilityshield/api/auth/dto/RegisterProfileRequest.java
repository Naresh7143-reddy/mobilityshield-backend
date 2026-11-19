package com.mobilityshield.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterProfileRequest {

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    private String fcmToken;

    public RegisterProfileRequest() {}

    public RegisterProfileRequest(String name, String fcmToken) {
        this.name = name;
        this.fcmToken = fcmToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
