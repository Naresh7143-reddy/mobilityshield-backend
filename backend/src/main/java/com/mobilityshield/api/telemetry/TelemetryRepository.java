package com.mobilityshield.api.telemetry;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mobilityshield.api.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class TelemetryRepository {

    private static final Logger log = LoggerFactory.getLogger(TelemetryRepository.class);

    private final Firestore firestore;

    public TelemetryRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Telemetry save(Telemetry telemetry) {
        if (telemetry.getTelemetryId() == null || telemetry.getTelemetryId().isBlank()) {
            throw new IllegalArgumentException("Telemetry ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.TELEMETRY_COLLECTION)
                    .document(telemetry.getTelemetryId());
            docRef.set(telemetry).get();
            return telemetry;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist telemetry [{}]: {}", telemetry.getTelemetryId(), e.getMessage());
            throw new RuntimeException("Failed to persist telemetry: " + e.getMessage(), e);
        }
    }

    public List<Telemetry> findByDeviceId(String deviceId, int limit) {
        List<Telemetry> list = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.TELEMETRY_COLLECTION)
                    .whereEqualTo("deviceId", deviceId)
                    .get().get();

            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                Telemetry telemetry = doc.toObject(Telemetry.class);
                if (telemetry != null) {
                    list.add(telemetry);
                }
            }
            // Sort by timestamp descending in-memory to avoid requiring Firestore composite indexes initially
            list.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });

            if (limit > 0 && list.size() > limit) {
                return list.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query telemetry for device [{}]: {}", deviceId, e.getMessage());
        }
        return list;
    }

    public Optional<Telemetry> findLatestByDeviceId(String deviceId) {
        List<Telemetry> list = findByDeviceId(deviceId, 1);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
