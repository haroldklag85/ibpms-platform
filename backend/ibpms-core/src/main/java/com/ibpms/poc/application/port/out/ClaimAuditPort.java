package com.ibpms.poc.application.port.out;

import com.ibpms.poc.domain.model.audit.ClaimAuditLog;

import java.util.List;
import java.util.UUID;

public interface ClaimAuditPort {
    void save(ClaimAuditLog auditLog);
    List<ClaimAuditLog> findByTaskId(UUID taskId);
}
