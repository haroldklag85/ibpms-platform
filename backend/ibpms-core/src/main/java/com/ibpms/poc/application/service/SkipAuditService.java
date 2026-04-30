package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.agile.SkipReason;
import com.ibpms.poc.infrastructure.jpa.entity.SkipAuditEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SkipAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SkipAuditService {

    private final SkipAuditRepository skipAuditRepository;
    private final AgileTaskService taskService;

    public SkipAuditService(SkipAuditRepository skipAuditRepository, AgileTaskService taskService) {
        this.skipAuditRepository = skipAuditRepository;
        this.taskService = taskService;
    }

    @Transactional
    public AgileTask skipTaskAndGetNext(UUID taskId, SkipReason reason, String details, String username, String tenantId) {
        // Enforce validation for OTHER reason
        if (reason == SkipReason.OTHER) {
            if (details == null || details.trim().length() < 10) {
                throw new IllegalArgumentException("Details must have at least 10 characters for OTHER reason.");
            }
        }

        // 1. Audit log
        SkipAuditEntity audit = new SkipAuditEntity();
        audit.setTaskId(taskId.toString());
        audit.setSkippedBy(username);
        audit.setTenantId(tenantId);
        audit.setReason(reason);
        audit.setDetails(details);
        skipAuditRepository.save(audit);

        // 2. Unclaim current task
        taskService.unclaimTask(taskId, username);

        // 3. Return next task sorted by SLA priority
        return taskService.claimNextTask(username);
    }
}
