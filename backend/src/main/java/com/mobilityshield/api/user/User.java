package com.mobilityshield.api.user;

import com.google.cloud.firestore.annotation.DocumentId;
import com.mobilityshield.api.security.Role;

import java.io.Serializable;
import java.time.Instant;

public class User implements Serializable {

    @DocumentId
    private String uid;
    private String name;
    private String email;
    private Role role = Role.USER;
    private String status = "ACTIVE";
    private String createdAt = Instant.now().toString();
    private String updatedAt = Instant.now().toString();
    private String fcmToken;

    public User() {}

    public User(String uid, String name, String email, Role role, String status, String fcmToken) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
        this.fcmToken = fcmToken;
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

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
