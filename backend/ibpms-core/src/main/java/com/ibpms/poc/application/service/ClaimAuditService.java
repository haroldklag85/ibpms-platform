package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.audit.ClaimAuditLog;
import com.ibpms.poc.domain.model.enums.ClaimActionType;
import com.ibpms.poc.application.port.out.ClaimAuditPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimAuditService {

    private final ClaimAuditPort claimAuditPort;

    public ClaimAuditService(ClaimAuditPort claimAuditPort) {
        this.claimAuditPort = claimAuditPort;
    }

    public void audit(UUID taskId, String userId, String actionType, String reason, String previousAssignee, String message) {
        ClaimAuditLog log = new ClaimAuditLog(
                taskId,
                userId,
                actionType,
                "default",
                Instant.now(),
                previousAssignee,
                reason,
                message
        );
        claimAuditPort.save(log);
    }

    public void auditForceUnclaim(UUID taskId, String supervisorId, String tenantId) {
        ClaimAuditLog log = new ClaimAuditLog(
                taskId,
                supervisorId,
                ClaimActionType.FORCE_UNCLAIMED.name(),
                tenantId,
                Instant.now(),
                null,
                null,
                null
        );
        claimAuditPort.save(log);
    }

    public List<ClaimAuditLog> getAuditTrail(UUID taskId) {
        return claimAuditPort.findByTaskId(taskId);
    }
}
