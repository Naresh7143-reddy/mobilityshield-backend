package com.mobilityshield.api.cybersecurity;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mobilityshield.api.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class SecurityAlertRepository {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertRepository.class);

    private final Firestore firestore;

    public SecurityAlertRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public SecurityAlert save(SecurityAlert alert) {
        if (alert.getAlertId() == null || alert.getAlertId().isBlank()) {
            throw new IllegalArgumentException("Alert ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.SECURITY_ALERTS_COLLECTION).document(alert.getAlertId());
            docRef.set(alert).get();
            log.info("Persisted security alert [{}] for device [{}] severity [{}]",
                    alert.getAlertId(), alert.getDeviceId(), alert.getSeverity());
            return alert;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist security alert [{}]: {}", alert.getAlertId(), e.getMessage());
            throw new RuntimeException("Failed to persist security alert: " + e.getMessage(), e);
        }
    }

    public Optional<SecurityAlert> findById(String alertId) {
        if (alertId == null || alertId.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot snapshot = firestore.collection(Constants.SECURITY_ALERTS_COLLECTION)
                    .document(alertId).get().get();
            if (snapshot.exists()) {
                return Optional.ofNullable(snapshot.toObject(SecurityAlert.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to find alert [{}]: {}", alertId, e.getMessage());
        }
        return Optional.empty();
    }

    public List<SecurityAlert> findByDeviceId(String deviceId) {
        List<SecurityAlert> alerts = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_ALERTS_COLLECTION)
                    .whereEqualTo("deviceId", deviceId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityAlert alert = doc.toObject(SecurityAlert.class);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
            alerts.sort((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query alerts for device [{}]: {}", deviceId, e.getMessage());
        }
        return alerts;
    }

    public List<SecurityAlert> findByUserId(String userId) {
        List<SecurityAlert> alerts = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_ALERTS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityAlert alert = doc.toObject(SecurityAlert.class);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
            alerts.sort((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query alerts for user [{}]: {}", userId, e.getMessage());
        }
        return alerts;
    }

    public List<SecurityAlert> findAll() {
        List<SecurityAlert> alerts = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_ALERTS_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityAlert alert = doc.toObject(SecurityAlert.class);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
            alerts.sort((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query all security alerts: {}", e.getMessage());
        }
        return alerts;
    }

    public void markAsRead(String alertId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("read", true);
            updates.put("status", "ACKNOWLEDGED");
            firestore.collection(Constants.SECURITY_ALERTS_COLLECTION).document(alertId).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to mark alert [{}] as read: {}", alertId, e.getMessage());
            throw new RuntimeException("Failed to mark alert as read: " + e.getMessage(), e);
        }
    }
}
