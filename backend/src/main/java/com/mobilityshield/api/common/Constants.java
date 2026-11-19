package com.mobilityshield.api.common;

public final class Constants {

    private Constants() {}

    // Firestore Collections
    public static final String USERS_COLLECTION = "users";
    public static final String DEVICES_COLLECTION = "devices";
    public static final String TELEMETRY_COLLECTION = "telemetry";
    public static final String SECURITY_EVENTS_COLLECTION = "security_events";
    public static final String SECURITY_ALERTS_COLLECTION = "security_alerts";
    public static final String LOGIN_ATTEMPTS_COLLECTION = "login_attempts";
    public static final String DEVICE_POLICIES_COLLECTION = "device_policies";
    public static final String AUDIT_LOGS_COLLECTION = "audit_logs";

    // Authentication & Security
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // User Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Device Statuses
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_QUARANTINED = "QUARANTINED";
    public static final String STATUS_PENDING = "PENDING";

    // Security Score Parameters
    public static final int MAX_SECURITY_SCORE = 100;
    public static final int MIN_SECURITY_SCORE = 0;
    public static final int DEDUCTION_ROOT_DETECTED = 30;
    public static final int DEDUCTION_UNSUPPORTED_OS = 15;
    public static final int DEDUCTION_SCREEN_LOCK_DISABLED = 20;
    public static final int DEDUCTION_POLICY_VIOLATION = 15;
    public static final int DEDUCTION_SUSPICIOUS_ACTIVITY = 20;

    // Login Attempts Quarantine Threshold
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;
}
