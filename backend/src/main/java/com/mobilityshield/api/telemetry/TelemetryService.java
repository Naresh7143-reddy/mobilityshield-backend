package com.mobilityshield.api.telemetry;

import com.mobilityshield.api.device.Device;
import com.mobilityshield.api.device.DeviceRepository;
import com.mobilityshield.api.exception.DeviceQuarantinedException;
import com.mobilityshield.api.exception.ResourceNotFoundException;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.telemetry.dto.SubmitTelemetryRequest;
import com.mobilityshield.api.telemetry.dto.TelemetryResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);

    private final TelemetryRepository telemetryRepository;
    private final DeviceRepository deviceRepository;
    private final SecurityContextService securityContextService;

    public TelemetryService(TelemetryRepository telemetryRepository,
                            DeviceRepository deviceRepository,
                            SecurityContextService securityContextService) {
        this.telemetryRepository = telemetryRepository;
        this.deviceRepository = deviceRepository;
        this.securityContextService = securityContextService;
    }

    public TelemetryResponseDto submitTelemetry(SubmitTelemetryRequest request) {
        String deviceId = request.getDeviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));

        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        if (Boolean.TRUE.equals(device.getQuarantineStatus()) || "QUARANTINED".equalsIgnoreCase(device.getStatus())) {
            throw new DeviceQuarantinedException("Telemetry submission blocked: Device [" + deviceId + "] is quarantined.");
        }

        String now = Instant.now().toString();
        String telemetryId = "tel-" + UUID.randomUUID().toString();

        Telemetry telemetry = new Telemetry();
        telemetry.setTelemetryId(telemetryId);
        telemetry.setDeviceId(deviceId);
        telemetry.setUserId(device.getUserId());
        telemetry.setBatteryPercentage(request.getBatteryPercentage());
        telemetry.setCharging(request.getCharging());
        telemetry.setAvailableRam(request.getAvailableRam());
        telemetry.setTotalRam(request.getTotalRam());
        telemetry.setAvailableStorage(request.getAvailableStorage());
        telemetry.setTotalStorage(request.getTotalStorage());
        telemetry.setNetworkType(request.getNetworkType());
        telemetry.setWifiConnected(request.getWifiConnected());
        telemetry.setTimestamp(now);

        Telemetry savedTelemetry = telemetryRepository.save(telemetry);

        // Update latest device telemetry snapshot and lastSeen
        device.setBatteryPercentage(request.getBatteryPercentage());
        device.setCharging(request.getCharging());
        if (request.getAvailableRam() != null) device.setAvailableRam(request.getAvailableRam());
        if (request.getTotalRam() != null) device.setTotalRam(request.getTotalRam());
        if (request.getAvailableStorage() != null) device.setAvailableStorage(request.getAvailableStorage());
        if (request.getTotalStorage() != null) device.setTotalStorage(request.getTotalStorage());
        if (request.getNetworkType() != null) device.setNetworkType(request.getNetworkType());
        if (request.getWifiConnected() != null) device.setWifiConnected(request.getWifiConnected());
        device.setLastSeen(now);
        deviceRepository.save(device);

        log.debug("Telemetry recorded for device [{}] with ID [{}]", deviceId, telemetryId);
        return TelemetryResponseDto.from(savedTelemetry);
    }

    public List<TelemetryResponseDto> getTelemetryHistory(String deviceId, int limit) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));
        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        return telemetryRepository.findByDeviceId(deviceId, limit).stream()
                .map(TelemetryResponseDto::from)
                .collect(Collectors.toList());
    }

    public TelemetryResponseDto getLatestTelemetry(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));
        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        return telemetryRepository.findLatestByDeviceId(deviceId)
                .map(TelemetryResponseDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("No telemetry records found for device ID: " + deviceId));
    }
}
