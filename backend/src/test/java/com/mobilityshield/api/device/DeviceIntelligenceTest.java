package com.mobilityshield.api.device;

import com.mobilityshield.api.device.dto.DeviceHealthResponseDto;
import com.mobilityshield.api.device.dto.DeviceResponseDto;
import com.mobilityshield.api.device.dto.EnrollDeviceRequest;
import com.mobilityshield.api.exception.DeviceQuarantinedException;
import com.mobilityshield.api.exception.ForbiddenException;
import com.mobilityshield.api.security.AuthenticatedUser;
import com.mobilityshield.api.security.Role;
import com.mobilityshield.api.security.SecurityContextService;
import com.mobilityshield.api.telemetry.Telemetry;
import com.mobilityshield.api.telemetry.TelemetryRepository;
import com.mobilityshield.api.telemetry.TelemetryService;
import com.mobilityshield.api.telemetry.dto.SubmitTelemetryRequest;
import com.mobilityshield.api.telemetry.dto.TelemetryResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceIntelligenceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private TelemetryRepository telemetryRepository;

    @Mock
    private SecurityContextService securityContextService;

    private DeviceService deviceService;
    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(deviceRepository, securityContextService);
        telemetryService = new TelemetryService(telemetryRepository, deviceRepository, securityContextService);
    }

    @Test
    @DisplayName("Enroll device binds verified UID and initializes status and score")
    void testEnrollDevice() {
        when(securityContextService.getCurrentUid()).thenReturn("user-42");
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));

        EnrollDeviceRequest request = new EnrollDeviceRequest();
        request.setDeviceName("Pixel 9 Pro");
        request.setManufacturer("Google");
        request.setModel("Pixel 9");
        request.setAndroidVersion("15.0");
        request.setSdkVersion(35);
        request.setBatteryPercentage(90);

        DeviceResponseDto enrolled = deviceService.enrollDevice(request);

        assertNotNull(enrolled);
        assertNotNull(enrolled.getDeviceId());
        assertTrue(enrolled.getDeviceId().startsWith("dev-"));
        assertEquals("user-42", enrolled.getUserId());
        assertEquals("ACTIVE", enrolled.getStatus());
        assertEquals(100, enrolled.getSecurityScore());
        assertFalse(enrolled.getQuarantineStatus());
    }

    @Test
    @DisplayName("Heartbeat fails with DeviceQuarantinedException if device is quarantined")
    void testHeartbeatQuarantined() {
        Device device = new Device();
        device.setDeviceId("dev-99");
        device.setUserId("user-42");
        device.setStatus("QUARANTINED");
        device.setQuarantineStatus(true);

        when(deviceRepository.findById("dev-99")).thenReturn(Optional.of(device));
        doNothing().when(securityContextService).assertOwnershipOrAdmin("user-42");

        assertThrows(DeviceQuarantinedException.class, () -> deviceService.recordHeartbeat("dev-99"));
    }

    @Test
    @DisplayName("Telemetry submission updates device snapshot and creates telemetry record")
    void testSubmitTelemetry() {
        Device device = new Device();
        device.setDeviceId("dev-10");
        device.setUserId("user-42");
        device.setStatus("ACTIVE");
        device.setQuarantineStatus(false);

        when(deviceRepository.findById("dev-10")).thenReturn(Optional.of(device));
        doNothing().when(securityContextService).assertOwnershipOrAdmin("user-42");
        when(telemetryRepository.save(any(Telemetry.class))).thenAnswer(i -> i.getArgument(0));
        when(deviceRepository.save(any(Device.class))).thenAnswer(i -> i.getArgument(0));

        SubmitTelemetryRequest request = new SubmitTelemetryRequest();
        request.setDeviceId("dev-10");
        request.setBatteryPercentage(75);
        request.setCharging(true);
        request.setAvailableRam(4000000000L);
        request.setTotalRam(8000000000L);
        request.setAvailableStorage(50000000000L);
        request.setTotalStorage(128000000000L);
        request.setNetworkType("WIFI");
        request.setWifiConnected(true);

        TelemetryResponseDto result = telemetryService.submitTelemetry(request);

        assertNotNull(result);
        assertEquals("dev-10", result.getDeviceId());
        assertEquals(75, result.getBatteryPercentage());
        assertTrue(result.getCharging());
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    @DisplayName("Device health evaluates low battery and offline status accurately")
    void testDeviceHealthEvaluation() {
        Device device = new Device();
        device.setDeviceId("dev-12");
        device.setUserId("user-42");
        device.setBatteryPercentage(8); // Critical (< 10%)
        device.setTotalRam(8000000000L);
        device.setAvailableRam(2000000000L); // 25% healthy
        device.setTotalStorage(100000000000L);
        device.setAvailableStorage(30000000000L); // 30% healthy
        device.setLastSeen(Instant.now().minusSeconds(1200).toString()); // 20 mins ago (offline)

        when(deviceRepository.findById("dev-12")).thenReturn(Optional.of(device));
        doNothing().when(securityContextService).assertOwnershipOrAdmin("user-42");

        DeviceHealthResponseDto health = deviceService.getDeviceHealth("dev-12");

        assertNotNull(health);
        assertEquals("CRITICAL", health.getBatteryStatus());
        assertEquals("OFFLINE", health.getConnectivityStatus());
        assertEquals("CRITICAL", health.getHealthStatus());
        assertFalse(health.getIssues().isEmpty());
    }
}
