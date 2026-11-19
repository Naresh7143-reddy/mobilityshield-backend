package com.mobilityshield.api.policy;

import com.mobilityshield.api.audit.AuditService;
import com.mobilityshield.api.policy.dto.UpdatePolicyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyRepository policyRepository;
    private final AuditService auditService;

    public PolicyService(PolicyRepository policyRepository, AuditService auditService) {
        this.policyRepository = policyRepository;
        this.auditService = auditService;
    }

    public List<DevicePolicy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public DevicePolicy getEffectivePolicy() {
        return policyRepository.getEffectivePolicy();
    }

    public DevicePolicy updatePolicy(UpdatePolicyRequest request, String adminUid) {
        String policyId = request.getPolicyId();
        if (policyId == null || policyId.isBlank()) {
            policyId = "pol-" + UUID.randomUUID().toString();
        }

        DevicePolicy policy = policyRepository.findById(policyId).orElse(new DevicePolicy());
        policy.setPolicyId(policyId);
        policy.setPolicyName(request.getPolicyName());
        policy.setDescription(request.getDescription());
        policy.setMinimumAndroidVersion(request.getMinimumAndroidVersion());
        policy.setRequireScreenLock(request.getRequireScreenLock());
        policy.setBlockRootedDevices(request.getBlockRootedDevices());
        policy.setMaximumFailedAttempts(request.getMaximumFailedAttempts());
        policy.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        policy.setUpdatedAt(Instant.now().toString());
        policy.setUpdatedBy(adminUid);

        DevicePolicy saved = policyRepository.save(policy);

        // Audit log
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("policyName", saved.getPolicyName());
        metadata.put("minOS", saved.getMinimumAndroidVersion());
        metadata.put("maxFailedAttempts", saved.getMaximumFailedAttempts());
        auditService.recordAction(adminUid, "UPDATE_DEVICE_POLICY", "POLICY",
                saved.getPolicyId(), "Updated device compliance policy: " + saved.getPolicyName(), metadata);

        log.info("Admin [{}] updated device policy [{}]", adminUid, saved.getPolicyId());
        return saved;
    }
}
