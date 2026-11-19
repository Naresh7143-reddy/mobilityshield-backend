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
public class LoginAttemptRepository {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptRepository.class);

    private final Firestore firestore;

    public LoginAttemptRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public LoginAttempt save(LoginAttempt attempt) {
        if (attempt.getAttemptId() == null || attempt.getAttemptId().isBlank()) {
            throw new IllegalArgumentException("Attempt ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.LOGIN_ATTEMPTS_COLLECTION).document(attempt.getAttemptId());
            docRef.set(attempt).get();
            return attempt;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist login attempt [{}]: {}", attempt.getAttemptId(), e.getMessage());
            throw new RuntimeException("Failed to persist login attempt: " + e.getMessage(), e);
        }
    }

    public List<LoginAttempt> findRecentByDeviceId(String deviceId, int limit) {
        List<LoginAttempt> attempts = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.LOGIN_ATTEMPTS_COLLECTION)
                    .whereEqualTo("deviceId", deviceId)
                    .get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                LoginAttempt attempt = doc.toObject(LoginAttempt.class);
                if (attempt != null) {
                    attempts.add(attempt);
                }
            }
            attempts.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            if (limit > 0 && attempts.size() > limit) {
                return attempts.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query login attempts for device [{}]: {}", deviceId, e.getMessage());
        }
        return attempts;
    }

    public int countConsecutiveFailuresForDevice(String deviceId) {
        List<LoginAttempt> attempts = findRecentByDeviceId(deviceId, 20);
        int consecutiveFailures = 0;
        for (LoginAttempt attempt : attempts) {
            if (Boolean.TRUE.equals(attempt.getSuccess())) {
                break; // Most recent successful login resets consecutive failed counter
            } else {
                consecutiveFailures++;
            }
        }
        return consecutiveFailures;
    }
}
