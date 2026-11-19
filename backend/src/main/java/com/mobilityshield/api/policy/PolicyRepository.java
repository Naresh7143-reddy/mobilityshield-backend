package com.mobilityshield.api.policy;

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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class PolicyRepository {

    private static final Logger log = LoggerFactory.getLogger(PolicyRepository.class);

    private final Firestore firestore;

    public PolicyRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public DevicePolicy save(DevicePolicy policy) {
        if (policy.getPolicyId() == null || policy.getPolicyId().isBlank()) {
            throw new IllegalArgumentException("Policy ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.DEVICE_POLICIES_COLLECTION).document(policy.getPolicyId());
            docRef.set(policy).get();
            log.info("Persisted device policy [{}]", policy.getPolicyId());
            return policy;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to persist device policy [{}]: {}", policy.getPolicyId(), e.getMessage());
            throw new RuntimeException("Failed to persist policy: " + e.getMessage(), e);
        }
    }

    public Optional<DevicePolicy> findById(String policyId) {
        try {
            DocumentSnapshot snapshot = firestore.collection(Constants.DEVICE_POLICIES_COLLECTION)
                    .document(policyId).get().get();
            if (snapshot.exists()) {
                return Optional.ofNullable(snapshot.toObject(DevicePolicy.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query policy [{}]: {}", policyId, e.getMessage());
        }
        return Optional.empty();
    }

    public List<DevicePolicy> findAll() {
        List<DevicePolicy> policies = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.DEVICE_POLICIES_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                DevicePolicy policy = doc.toObject(DevicePolicy.class);
                if (policy != null) {
                    policies.add(policy);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to query all device policies: {}", e.getMessage());
        }
        return policies;
    }

    public DevicePolicy getEffectivePolicy() {
        List<DevicePolicy> policies = findAll();
        for (DevicePolicy p : policies) {
            if (Boolean.TRUE.equals(p.getEnabled())) {
                return p;
            }
        }
        // Fallback default enterprise baseline policy if none explicitly defined in Firestore
        DevicePolicy defaultPolicy = new DevicePolicy();
        defaultPolicy.setPolicyId("default-enterprise-policy");
        defaultPolicy.setPolicyName("Default Enterprise Baseline");
        defaultPolicy.setDescription("Standard baseline compliance policy");
        defaultPolicy.setMinimumAndroidVersion("12.0");
        defaultPolicy.setRequireScreenLock(true);
        defaultPolicy.setBlockRootedDevices(true);
        defaultPolicy.setMaximumFailedAttempts(3);
        defaultPolicy.setEnabled(true);
        return defaultPolicy;
    }
}
