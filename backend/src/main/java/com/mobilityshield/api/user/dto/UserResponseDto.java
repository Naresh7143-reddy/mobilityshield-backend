package com.mobilityshield.api.user.dto;

import com.mobilityshield.api.security.Role;
import com.mobilityshield.api.user.User;

public class UserResponseDto {

    private String uid;
    private String name;
    private String email;
    private Role role;
    private String status;
    private String createdAt;
    private String updatedAt;

    public UserResponseDto() {}

    public UserResponseDto(User user) {
        if (user != null) {
            this.uid = user.getUid();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
            this.status = user.getStatus();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }
    }

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
