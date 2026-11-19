package com.mobilityshield.api.device;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mobilityshield.api.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class DeviceRepository {

    private static final Logger log = LoggerFactory.getLogger(DeviceRepository.class);

    private final Firestore firestore;

    public DeviceRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Device save(Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.DEVICES_COLLECTION).document(device.getDeviceId());
            docRef.set(device).get();
            log.info("Persisted device [{}] for user [{}]", device.getDeviceId(), device.getUserId());
            return device;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist device [{}]: {}", device.getDeviceId(), e.getMessage());
            throw new RuntimeException("Failed to persist device: " + e.getMessage(), e);
        }
    }

    public Optional<Device> findById(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot snapshot = firestore.collection(Constants.DEVICES_COLLECTION).document(deviceId).get().get();
            if (snapshot.exists()) {
                return Optional.ofNullable(snapshot.toObject(Device.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to find device [{}]: {}", deviceId, e.getMessage());
        }
        return Optional.empty();
    }

    public List<Device> findByUserId(String userId) {
        List<Device> devices = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.DEVICES_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                Device device = doc.toObject(Device.class);
                if (device != null) {
                    devices.add(device);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query devices for user [{}]: {}", userId, e.getMessage());
        }
        return devices;
    }

    public List<Device> findAll() {
        List<Device> devices = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.DEVICES_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                Device device = doc.toObject(Device.class);
                if (device != null) {
                    devices.add(device);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to fetch all devices: {}", e.getMessage());
        }
        return devices;
    }

    public void updateHeartbeat(String deviceId, String timestamp) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastSeen", timestamp);
            firestore.collection(Constants.DEVICES_COLLECTION).document(deviceId).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to update heartbeat for device [{}]: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to update heartbeat: " + e.getMessage(), e);
        }
    }

    public void updateStatusAndQuarantine(String deviceId, String status, Boolean quarantined, String reason) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("quarantineStatus", quarantined);
            updates.put("quarantineReason", reason);
            updates.put("lastSeen", Instant.now().toString());
            firestore.collection(Constants.DEVICES_COLLECTION).document(deviceId).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to update status for device [{}]: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to update device status: " + e.getMessage(), e);
        }
    }

    public void updateSecurityScore(String deviceId, int score) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("securityScore", score);
            firestore.collection(Constants.DEVICES_COLLECTION).document(deviceId).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to update security score for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    public void deleteById(String deviceId) {
        try {
            firestore.collection(Constants.DEVICES_COLLECTION).document(deviceId).delete().get();
            log.info("Deleted device [{}]", deviceId);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to delete device [{}]: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to delete device: " + e.getMessage(), e);
        }
    }
}
