package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import com.ibpms.poc.infrastructure.persistence.ClaimAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimAuditService {

    private final ClaimAuditLogRepository auditRepository;

    public ClaimAuditService(ClaimAuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void auditForceUnclaim(UUID taskId, String supervisorId, String tenantId) {
        ClaimAuditLog log = new ClaimAuditLog(
                taskId,
                supervisorId,
                "FORCE_UNCLAIM",
                tenantId,
                Instant.now()
        );
        auditRepository.save(log);
    }

    public List<ClaimAuditLog> getAuditTrail(UUID taskId) {
        return auditRepository.findByTaskIdOrderByTimestampDesc(taskId);
    }
}
