package com.mobilityshield.api.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
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
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Optional<User> findById(String uid) {
        if (uid == null || uid.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot snapshot = firestore.collection(Constants.USERS_COLLECTION)
                    .document(uid).get().get();
            if (snapshot.exists()) {
                return Optional.ofNullable(snapshot.toObject(User.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Error retrieving user with uid [{}]: {}", uid, e.getMessage());
        }
        return Optional.empty();
    }

    public User save(User user) {
        if (user.getUid() == null || user.getUid().isBlank()) {
            throw new IllegalArgumentException("User UID must not be null or blank");
        }
        user.setUpdatedAt(Instant.now().toString());
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now().toString());
        }

        try {
            DocumentReference docRef = firestore.collection(Constants.USERS_COLLECTION).document(user.getUid());
            docRef.set(user).get();
            log.info("Persisted user [{}] with role [{}]", user.getUid(), user.getRole());
            return user;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist user [{}]: {}", user.getUid(), e.getMessage());
            throw new RuntimeException("Failed to persist user: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(Constants.USERS_COLLECTION).get();
            for (DocumentSnapshot doc : future.get().getDocuments()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Error fetching all users: {}", e.getMessage());
        }
        return users;
    }

    public void updateStatus(String uid, String status) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", Instant.now().toString());
            firestore.collection(Constants.USERS_COLLECTION).document(uid).update(updates).get();
            log.info("Updated status for user [{}] to [{}]", uid, status);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to update status for user [{}]: {}", uid, e.getMessage());
            throw new RuntimeException("Failed to update user status: " + e.getMessage(), e);
        }
    }

    public void updateFcmToken(String uid, String fcmToken) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", fcmToken);
            updates.put("updatedAt", Instant.now().toString());
            firestore.collection(Constants.USERS_COLLECTION).document(uid).update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to update FCM token for user [{}]: {}", uid, e.getMessage());
        }
    }
}
