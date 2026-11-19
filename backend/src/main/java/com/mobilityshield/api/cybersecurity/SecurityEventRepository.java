package com.mobilityshield.api.cybersecurity;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mobilityshield.api.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class SecurityEventRepository {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventRepository.class);

    private final Firestore firestore;

    public SecurityEventRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public SecurityEvent save(SecurityEvent event) {
        if (event.getEventId() == null || event.getEventId().isBlank()) {
            throw new IllegalArgumentException("Event ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.SECURITY_EVENTS_COLLECTION).document(event.getEventId());
            docRef.set(event).get();
            return event;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist security event [{}]: {}", event.getEventId(), e.getMessage());
            throw new RuntimeException("Failed to persist security event: " + e.getMessage(), e);
        }
    }

    public List<SecurityEvent> findByDeviceId(String deviceId, int limit) {
        List<SecurityEvent> events = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_EVENTS_COLLECTION)
                    .whereEqualTo("deviceId", deviceId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityEvent event = doc.toObject(SecurityEvent.class);
                if (event != null) {
                    events.add(event);
                }
            }
            events.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            if (limit > 0 && events.size() > limit) {
                return events.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query security events for device [{}]: {}", deviceId, e.getMessage());
        }
        return events;
    }

    public List<SecurityEvent> findByUserId(String userId, int limit) {
        List<SecurityEvent> events = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_EVENTS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityEvent event = doc.toObject(SecurityEvent.class);
                if (event != null) {
                    events.add(event);
                }
            }
            events.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            if (limit > 0 && events.size() > limit) {
                return events.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query security events for user [{}]: {}", userId, e.getMessage());
        }
        return events;
    }

    public List<SecurityEvent> findAll(int limit) {
        List<SecurityEvent> events = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.SECURITY_EVENTS_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                SecurityEvent event = doc.toObject(SecurityEvent.class);
                if (event != null) {
                    events.add(event);
                }
            }
            events.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            if (limit > 0 && events.size() > limit) {
                return events.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query all security events: {}", e.getMessage());
        }
        return events;
    }
}
