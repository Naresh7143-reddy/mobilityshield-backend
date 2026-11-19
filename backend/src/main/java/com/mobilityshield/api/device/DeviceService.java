package com.mobilityshield.api.device;

import com.mobilityshield.api.device.dto.DeviceHealthResponseDto;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.device.dto.EnrollDeviceRequest;
import com.mobilityshield.api.exception.DeviceQuarantinedException;
import com.mobilityshield.api.exception.ResourceNotFoundException;
import com.mobilityshield.api.security.SecurityContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final SecurityContextService securityContextService;

    public DeviceService(DeviceRepository deviceRepository, SecurityContextService securityContextService) {
        this.deviceRepository = deviceRepository;
        this.securityContextService = securityContextService;
    }

    public DeviceResponseDto enrollDevice(EnrollDeviceRequest request) {
        String currentUid = securityContextService.getCurrentUid();
        String deviceId = "dev-" + UUID.randomUUID().toString();
        String now = Instant.now().toString();

        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setUserId(currentUid);
        device.setDeviceName(request.getDeviceName());
        device.setManufacturer(request.getManufacturer());
        device.setModel(request.getModel());
        device.setAndroidVersion(request.getAndroidVersion());
        device.setSdkVersion(request.getSdkVersion());
        device.setSecurityPatchLevel(request.getSecurityPatchLevel());
        device.setBatteryPercentage(request.getBatteryPercentage());
        device.setCharging(request.getCharging());
        device.setTotalRam(request.getTotalRam());
        device.setAvailableRam(request.getAvailableRam());
        device.setTotalStorage(request.getTotalStorage());
        device.setAvailableStorage(request.getAvailableStorage());
        device.setNetworkType(request.getNetworkType());
        device.setWifiConnected(request.getWifiConnected());
        device.setEnrolledAt(now);
        device.setLastSeen(now);
        device.setStatus("ACTIVE");
        device.setSecurityScore(100);
        device.setQuarantineStatus(false);
        device.setQuarantineReason(null);

        Device saved = deviceRepository.save(device);
        log.info("Enrolled device [{}] for user [{}]", deviceId, currentUid);
        return DeviceResponseDto.from(saved);
    }

    public List<DeviceResponseDto> getDevices() {
        if (securityContextService.isAdmin()) {
            return deviceRepository.findAll().stream()
                    .map(DeviceResponseDto::from)
                    .collect(Collectors.toList());
        }
        String currentUid = securityContextService.getCurrentUid();
        return deviceRepository.findByUserId(currentUid).stream()
                .map(DeviceResponseDto::from)
                .collect(Collectors.toList());
    }

    public DeviceResponseDto getDeviceById(String deviceId) {
        Device device = getDeviceEntity(deviceId);
        securityContextService.assertOwnershipOrAdmin(device.getUserId());
        return DeviceResponseDto.from(device);
    }

    public Device getDeviceEntity(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with ID: " + deviceId));
    }

    public void deregisterDevice(String deviceId) {
        Device device = getDeviceEntity(deviceId);
        securityContextService.assertOwnershipOrAdmin(device.getUserId());
        deviceRepository.deleteById(deviceId);
        log.info("Deregistered device [{}] by user [{}]", deviceId, securityContextService.getCurrentUid());
    }

    public void recordHeartbeat(String deviceId) {
        Device device = getDeviceEntity(deviceId);
        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        if (Boolean.TRUE.equals(device.getQuarantineStatus()) || "QUARANTINED".equalsIgnoreCase(device.getStatus())) {
            throw new DeviceQuarantinedException("Heartbeat rejected: Device [" + deviceId + "] is quarantined.");
        }

        String now = Instant.now().toString();
        deviceRepository.updateHeartbeat(deviceId, now);
        log.debug("Recorded heartbeat for device [{}] at {}", deviceId, now);
    }

    public DeviceHealthResponseDto getDeviceHealth(String deviceId) {
        Device device = getDeviceEntity(deviceId);
        securityContextService.assertOwnershipOrAdmin(device.getUserId());

        List<String> issues = new ArrayList<>();

        // 1. Connectivity status (Online if heartbeat within 10 minutes)
        String connectivityStatus = "ONLINE";
        try {
            Instant lastSeenInstant = Instant.parse(device.getLastSeen());
            if (Duration.between(lastSeenInstant, Instant.now()).toMinutes() > 10) {
                connectivityStatus = "OFFLINE";
                issues.add("Device has been offline for more than 10 minutes");
            }
        } catch (Exception e) {
            connectivityStatus = "UNKNOWN";
        }

        // 2. Battery status
        String batteryStatus = "HEALTHY";
        if (device.getBatteryPercentage() != null) {
            if (device.getBatteryPercentage() < 10) {
                batteryStatus = "CRITICAL";
                issues.add("Battery level critically low (" + device.getBatteryPercentage() + "%)");
            } else if (device.getBatteryPercentage() < 20) {
                batteryStatus = "LOW";
                issues.add("Battery level low (" + device.getBatteryPercentage() + "%)");
            }
        }

        // 3. RAM status
        String ramStatus = "HEALTHY";
        if (device.getTotalRam() != null && device.getAvailableRam() != null && device.getTotalRam() > 0) {
            double freePercent = ((double) device.getAvailableRam() / device.getTotalRam()) * 100.0;
            if (freePercent < 5.0) {
                ramStatus = "CRITICAL";
                issues.add("Available RAM critically low (" + String.format("%.1f", freePercent) + "%)");
            } else if (freePercent < 15.0) {
                ramStatus = "HIGH_USAGE";
                issues.add("Available RAM low (" + String.format("%.1f", freePercent) + "%)");
            }
        }

        // 4. Storage status
        String storageStatus = "HEALTHY";
        if (device.getTotalStorage() != null && device.getAvailableStorage() != null && device.getTotalStorage() > 0) {
            double freeStoragePercent = ((double) device.getAvailableStorage() / device.getTotalStorage()) * 100.0;
            if (freeStoragePercent < 5.0) {
                storageStatus = "CRITICAL";
                issues.add("Available storage critically low (" + String.format("%.1f", freeStoragePercent) + "%)");
            } else if (freeStoragePercent < 15.0) {
                storageStatus = "LOW_SPACE";
                issues.add("Available storage low (" + String.format("%.1f", freeStoragePercent) + "%)");
            }
        }

        // 5. Aggregate overall health
        String overallHealth;
        if ("CRITICAL".equals(batteryStatus) || "CRITICAL".equals(ramStatus) || "CRITICAL".equals(storageStatus)
                || Boolean.TRUE.equals(device.getQuarantineStatus())) {
            overallHealth = "CRITICAL";
        } else if ("LOW".equals(batteryStatus) || "HIGH_USAGE".equals(ramStatus) || "LOW_SPACE".equals(storageStatus)
                || "OFFLINE".equals(connectivityStatus)) {
            overallHealth = "WARNING";
        } else if (issues.isEmpty()) {
            overallHealth = "EXCELLENT";
        } else {
            overallHealth = "GOOD";
        }

        return new DeviceHealthResponseDto(deviceId, overallHealth, batteryStatus, ramStatus,
                storageStatus, connectivityStatus, device.getLastSeen(), issues);
    }
}
