package com.mobilityshield.api.admin;

import com.mobilityshield.api.admin.dto.AdminDashboardDto;
import com.mobilityshield.api.admin.dto.AuditLogResponseDto;
import com.mobilityshield.api.admin.dto.QuarantineDeviceRequest;
import com.mobilityshield.api.admin.dto.RecoverDeviceRequest;
import com.mobilityshield.api.admin.dto.UpdateDeviceStatusRequest;
import com.mobilityshield.api.admin.dto.UpdateUserStatusRequest;
import com.mobilityshield.api.common.ApiResponse;
import com.mobilityshield.api.cybersecurity.dto.SecurityAlertResponseDto;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.policy.DevicePolicy;
import com.mobilityshield.api.policy.PolicyService;
import com.mobilityshield.api.policy.dto.UpdatePolicyRequest;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final PolicyService policyService;
    private final SecurityContextService securityContextService;

    public AdminController(AdminService adminService,
                           PolicyService policyService,
                           SecurityContextService securityContextService) {
        this.adminService = adminService;
        this.policyService = policyService;
        this.securityContextService = securityContextService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboardStats() {
        AdminDashboardDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved successfully", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsers() {
        List<UserResponseDto> users = adminService.getUsers();
        return ResponseEntity.ok(ApiResponse.success("User inventory retrieved successfully", users));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceResponseDto>>> getDevices() {
        List<DeviceResponseDto> devices = adminService.getDevices();
        return ResponseEntity.ok(ApiResponse.success("Device inventory retrieved successfully", devices));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SecurityAlertResponseDto>>> getAlerts() {
        List<SecurityAlertResponseDto> alerts = adminService.getAlerts();
        return ResponseEntity.ok(ApiResponse.success("Security alerts retrieved successfully", alerts));
    }

    @PatchMapping("/users/{uid}/status")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(
            @PathVariable String uid,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        UserResponseDto updated = adminService.updateUserStatus(uid, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", updated));
    }

    @PostMapping("/devices/{deviceId}/quarantine")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> quarantineDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody QuarantineDeviceRequest request) {
        DeviceResponseDto quarantined = adminService.quarantineDevice(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("Device quarantined successfully", quarantined));
    }

    @PostMapping("/devices/{deviceId}/recover")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> recoverDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody RecoverDeviceRequest request) {
        DeviceResponseDto recovered = adminService.recoverDevice(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("Device recovered from quarantine successfully", recovered));
    }

    @PatchMapping("/devices/{deviceId}/status")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> updateDeviceStatus(
            @PathVariable String deviceId,
            @Valid @RequestBody UpdateDeviceStatusRequest request) {
        DeviceResponseDto updated = adminService.updateDeviceStatus(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("Device status updated successfully", updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponseDto>>> getAuditLogs(
            @RequestParam(defaultValue = "100") int limit) {
        List<AuditLogResponseDto> logs = adminService.getAuditLogs(limit);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    @GetMapping("/policies")
    public ResponseEntity<ApiResponse<List<DevicePolicy>>> getPolicies() {
        List<DevicePolicy> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success("Policies retrieved successfully", policies));
    }

    @PutMapping("/policies")
    public ResponseEntity<ApiResponse<DevicePolicy>> updatePolicy(
            @Valid @RequestBody UpdatePolicyRequest request) {
        String adminUid = securityContextService.getCurrentUid();
        DevicePolicy updated = policyService.updatePolicy(request, adminUid);
        return ResponseEntity.ok(ApiResponse.success("Policy saved successfully", updated));
    }
}
