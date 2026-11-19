package com.mobilityshield.api.cybersecurity;

import com.mobilityshield.api.common.ApiResponse;
import com.mobilityshield.api.cybersecurity.dto.RecordLoginAttemptRequest;
import com.mobilityshield.api.cybersecurity.dto.RecordSecurityEventRequest;
import com.mobilityshield.api.cybersecurity.dto.SecurityAlertResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityComplianceResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityEventResponseDto;
import com.mobilityshield.api.cybersecurity.dto.SecurityScoreResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    private final SecurityService securityService;
    private final LoginAttemptService loginAttemptService;

    public SecurityController(SecurityService securityService,
                              LoginAttemptService loginAttemptService) {
        this.securityService = securityService;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping("/score/{deviceId}")
    public ResponseEntity<ApiResponse<SecurityScoreResponseDto>> getSecurityScore(
            @PathVariable String deviceId) {
        SecurityScoreResponseDto score = securityService.getSecurityScore(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Security score evaluated successfully", score));
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<SecurityEventResponseDto>> recordSecurityEvent(
            @Valid @RequestBody RecordSecurityEventRequest request) {
        SecurityEventResponseDto event = securityService.recordSecurityEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Security event recorded successfully", event));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<SecurityEventResponseDto>>> getSecurityEvents(
            @RequestParam(required = false) String deviceId,
            @RequestParam(defaultValue = "50") int limit) {
        List<SecurityEventResponseDto> events = securityService.getSecurityEvents(deviceId, limit);
        return ResponseEntity.ok(ApiResponse.success("Security events retrieved successfully", events));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SecurityAlertResponseDto>>> getSecurityAlerts(
            @RequestParam(required = false) String deviceId) {
        List<SecurityAlertResponseDto> alerts = securityService.getSecurityAlerts(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Security alerts retrieved successfully", alerts));
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<ApiResponse<SecurityAlertResponseDto>> getSecurityAlertById(
            @PathVariable String alertId) {
        SecurityAlertResponseDto alert = securityService.getSecurityAlertById(alertId);
        return ResponseEntity.ok(ApiResponse.success("Security alert retrieved successfully", alert));
    }

    @PatchMapping("/alerts/{alertId}/read")
    public ResponseEntity<ApiResponse<Void>> markAlertAsRead(
            @PathVariable String alertId) {
        securityService.markAlertAsRead(alertId);
        return ResponseEntity.ok(ApiResponse.success("Security alert marked as read", null));
    }

    @GetMapping("/compliance/{deviceId}")
    public ResponseEntity<ApiResponse<SecurityComplianceResponseDto>> getDeviceCompliance(
            @PathVariable String deviceId) {
        SecurityComplianceResponseDto compliance = securityService.getDeviceCompliance(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device compliance checked successfully", compliance));
    }

    @PostMapping("/login-attempts")
    public ResponseEntity<ApiResponse<Void>> recordLoginAttempt(
            @Valid @RequestBody RecordLoginAttemptRequest request) {
        loginAttemptService.recordLoginAttempt(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Login attempt processed successfully", null));
    }
}
