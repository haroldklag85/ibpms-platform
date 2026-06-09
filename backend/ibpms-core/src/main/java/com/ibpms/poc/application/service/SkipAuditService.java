package com.ibpms.poc.application.service;

import com.ibpms.poc.domain.model.agile.AgileTask;
import com.ibpms.poc.domain.model.agile.SkipReason;
import com.ibpms.poc.infrastructure.jpa.entity.SkipAuditEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SkipAuditRepository;
import com.ibpms.poc.crosscutting.annotations.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de auditoría para operaciones Skip Task (Saltar Tareas).
 * Extrae la lógica de persistencia del TaskSkipController.
 * 
 * @Traceability(US = "US-008", CA = {"CA-02"})
 */
@Service
@Traceability(US = "US-008", CA = {"CA-02"})
public class SkipAuditService {

    private final SkipAuditRepository skipAuditRepository;
    private final AgileTaskService taskService;
    private final com.ibpms.poc.infrastructure.jpa.repository.SystemAuditLogRepository systemAuditLogRepository;

    public SkipAuditService(SkipAuditRepository skipAuditRepository, AgileTaskService taskService, com.ibpms.poc.infrastructure.jpa.repository.SystemAuditLogRepository systemAuditLogRepository) {
        this.skipAuditRepository = skipAuditRepository;
        this.taskService = taskService;
        this.systemAuditLogRepository = systemAuditLogRepository;
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

        com.ibpms.poc.infrastructure.jpa.entity.SystemAuditLogEntity systemLog = new com.ibpms.poc.infrastructure.jpa.entity.SystemAuditLogEntity(username, "TASK_SKIPPED", 0, taskId.toString(), reason.name() + " - " + details);
        systemAuditLogRepository.save(systemLog);

        // 2. Unclaim current task
        taskService.unclaimTask(taskId, username, details);

        // 3. Return next task sorted by SLA priority
        return taskService.claimNextTask(username);
    }
    /**
     * Retorna el histórico completo de skips (uso administrativo/auditoría).
     */
    // @Traceability: US-008 - CA-02 (ADR-001 Refactor)
    public java.util.List<SkipAuditEntity> findAll() {
        return skipAuditRepository.findAll();
    }
}
