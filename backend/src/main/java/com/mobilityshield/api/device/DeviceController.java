package com.mobilityshield.api.device;

import com.mobilityshield.api.common.ApiResponse;
import com.mobilityshield.api.device.dto.DeviceHealthResponseDto;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.device.dto.EnrollDeviceRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> enrollDevice(@Valid @RequestBody EnrollDeviceRequest request) {
        DeviceResponseDto enrolled = deviceService.enrollDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Device enrolled successfully", enrolled));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceResponseDto>>> getDevices() {
        List<DeviceResponseDto> devices = deviceService.getDevices();
        return ResponseEntity.ok(ApiResponse.success("Devices retrieved successfully", devices));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceResponseDto>> getDeviceById(@PathVariable String deviceId) {
        DeviceResponseDto device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device retrieved successfully", device));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deregisterDevice(@PathVariable String deviceId) {
        deviceService.deregisterDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device deregistered successfully", null));
    }

    @PostMapping("/{deviceId}/heartbeat")
    public ResponseEntity<ApiResponse<Void>> recordHeartbeat(@PathVariable String deviceId) {
        deviceService.recordHeartbeat(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Heartbeat recorded successfully", null));
    }

    @GetMapping("/{deviceId}/health")
    public ResponseEntity<ApiResponse<DeviceHealthResponseDto>> getDeviceHealth(@PathVariable String deviceId) {
        DeviceHealthResponseDto health = deviceService.getDeviceHealth(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device health evaluated successfully", health));
    }
}
