package com.mobilityshield.api.audit;

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
public class AuditRepository {

    private static final Logger log = LoggerFactory.getLogger(AuditRepository.class);

    private final Firestore firestore;

    public AuditRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public AuditLog save(AuditLog auditLog) {
        if (auditLog.getLogId() == null || auditLog.getLogId().isBlank()) {
            throw new IllegalArgumentException("Audit log ID must not be null or blank");
        }
        try {
            DocumentReference docRef = firestore.collection(Constants.AUDIT_LOGS_COLLECTION)
                    .document(auditLog.getLogId());
            docRef.set(auditLog).get();
            log.info("Recorded audit log [{}]: action [{}] on [{}::{}] by admin [{}]",
                    auditLog.getLogId(), auditLog.getAction(), auditLog.getTargetType(),
                    auditLog.getTargetId(), auditLog.getAdminUid());
            return auditLog;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to save audit log [{}]: {}", auditLog.getLogId(), e.getMessage());
            throw new RuntimeException("Failed to save audit log: " + e.getMessage(), e);
        }
    }

    public List<AuditLog> findAll(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(Constants.AUDIT_LOGS_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                AuditLog item = doc.toObject(AuditLog.class);
                if (item != null) {
                    logs.add(item);
                }
            }
            logs.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });
            if (limit > 0 && logs.size() > limit) {
                return logs.subList(0, limit);
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to fetch audit logs: {}", e.getMessage());
        }
        return logs;
    }
}
