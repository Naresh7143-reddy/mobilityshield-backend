package com.mobilityshield.api.telemetry;

import com.mobilityshield.api.common.ApiResponse;
import com.mobilityshield.api.telemetry.dto.SubmitTelemetryRequest;
import com.mobilityshield.api.telemetry.dto.TelemetryResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TelemetryResponseDto>> submitTelemetry(
            @Valid @RequestBody SubmitTelemetryRequest request) {
        TelemetryResponseDto telemetry = telemetryService.submitTelemetry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Telemetry submitted successfully", telemetry));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<List<TelemetryResponseDto>>> getTelemetryHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "50") int limit) {
        List<TelemetryResponseDto> history = telemetryService.getTelemetryHistory(deviceId, limit);
        return ResponseEntity.ok(ApiResponse.success("Telemetry history retrieved successfully", history));
    }

    @GetMapping("/{deviceId}/latest")
    public ResponseEntity<ApiResponse<TelemetryResponseDto>> getLatestTelemetry(
            @PathVariable String deviceId) {
        TelemetryResponseDto latest = telemetryService.getLatestTelemetry(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Latest telemetry retrieved successfully", latest));
    }
}
