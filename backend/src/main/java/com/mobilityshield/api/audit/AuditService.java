package com.mobilityshield.api.audit;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public AuditLog recordAction(String adminUid, String action, String targetType,
                                 String targetId, String description, Map<String, Object> metadata) {
        String logId = "log-" + UUID.randomUUID().toString();
        AuditLog log = new AuditLog(logId, adminUid, action, targetType, targetId, description, metadata);
        return auditRepository.save(log);
    }

    public List<AuditLog> getAuditLogs(int limit) {
        return auditRepository.findAll(limit);
    }
}
