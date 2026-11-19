package com.mobilityshield.api.security;

import java.io.Serializable;

public class AuthenticatedUser implements Serializable {

    private final String uid;
    private final String email;
    private final Role role;
    private final String status;

    public AuthenticatedUser(String uid, String email, Role role, String status) {
        this.uid = uid;
        this.email = email;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : "ACTIVE";
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }
}
