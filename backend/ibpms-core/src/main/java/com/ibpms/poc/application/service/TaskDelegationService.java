package com.ibpms.poc.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.TaskService;
import com.ibpms.poc.application.port.out.AuditLogPort;
import java.util.UUID;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.ibpms.poc.infrastructure.jpa.repository.IbpmsProfileRepository;

/**
 * Servicio para procesar Delegaciones In-Flight con evaluación perezosa (Lazy Evaluation).
 * Cubre el Criterio CA-23 sin requerir CRON (CA-24).
 */
@Service
public class TaskDelegationService {
    private static final Logger log = LoggerFactory.getLogger(TaskDelegationService.class);

    private final IbpmsProfileRepository profileRepository;
    private final com.ibpms.poc.infrastructure.jpa.repository.UserDelegationRepository delegationRepository;
    private final TaskService taskService;
    private final AuditLogPort auditLogPort;

    public TaskDelegationService(IbpmsProfileRepository profileRepository, com.ibpms.poc.infrastructure.jpa.repository.UserDelegationRepository delegationRepository, TaskService taskService, AuditLogPort auditLogPort) {
        this.profileRepository = profileRepository;
        this.delegationRepository = delegationRepository;
        this.taskService = taskService;
        this.auditLogPort = auditLogPort;
    }
    /**
     * Revisa si una tarea pertenece a una delegación vigente o si ya expiró.
     * Llamado internamente por el Controller del Workdesk (GET).
     * 
     * @param originalOwner   Usuario dueño original
     * @param currentAssignee Usuario asignado actual (suplente)
     * @param expiryDate      Fecha de expiración de la delegación
     * @param taskId          ID de la tarea
     * @return El assignee efectivo. Puede haber revertido On-the-fly.
     */
    // @Traceability: US-036 - CA-23
    public String evaluateAndRevertTaskIfNeeded(String originalOwner, String currentAssignee, LocalDateTime expiryDate, String taskId) {
        // En una implementación real, esto también verificaría el estado de la BD.
        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
            if (!originalOwner.equals(currentAssignee)) {
                log.info("Lazy Evaluation CA-23: La delegación para la tarea {} ha expirado. Revirtiendo On-the-fly a {}.", taskId, originalOwner);
                
                try {
                    // Revertimos on-the-fly al dueño original usando el motor BPMN
                    taskService.setAssignee(taskId, originalOwner);
                    
                    // CA-23: Registro de la auditoría indeleble
                    auditLogPort.saveAuditLog(
                        UUID.randomUUID().toString(),
                        "TASK_DELEGATION",
                        taskId,
                        "REVERT_DELEGATION",
                        "SYSTEM",
                        java.time.LocalDateTime.now(),
                        null,
                        false,
                        false,
                        String.format("{\"originalOwner\":\"%s\",\"expiredAssignee\":\"%s\"}", originalOwner, currentAssignee)
                    );
                    log.warn("SUDO Action [Audit Trail]: Retorno automático de tarea In-Flight post-delegación. Tarea: {}, Nuevo Asignado: {}", taskId, originalOwner);
                } catch (Exception e) {
                    log.error("Guardrail CA-23: Error al intentar revertir la asignación en Camunda. No bloqueamos la transacción.", e);
                }
                
                return originalOwner;
            }
        }
        return currentAssignee;
    }

    /**
     * CA-15: Validación Perimetral RBAC Anti-IDOR.
     */
    public String validateDelegationHierarchy(String executiveUserId, String assistantUserId, String tenantId) {
        log.info("CA-15 RBAC Perimeter Check: Executive={} requesting delegation view for Assistant={} in Tenant={}",
                executiveUserId, assistantUserId, tenantId);

        // VALIDACIÓN 1: Self-delegation is a no-op
        if (executiveUserId.equals(assistantUserId)) {
            log.warn("CA-15: Self-delegation attempted. Returning own context.");
            return executiveUserId; 
        }

        boolean isAuthorized = checkDelegationAuthority(executiveUserId, assistantUserId, tenantId);

        if (!isAuthorized) {
            log.error("CA-15 IDOR BLOCKED: User {} attempted unauthorized delegation view of {} in tenant {}. " +
                      "SUDO Action [Audit Trail]: Potential IDOR attack vector detected.",
                      executiveUserId, assistantUserId, tenantId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Acceso denegado: No tiene autorización jerárquica para visualizar el escritorio de este usuario.");
        }

        log.info("CA-15: Delegation hierarchy validated. Executive={} → Assistant={}", executiveUserId, assistantUserId);
        return resolveDisplayName(assistantUserId);
    }

    private boolean checkDelegationAuthority(String executiveId, String assistantId, String tenantId) {
        return delegationRepository.findBySupervisorIdAndAssistantIdAndTenantId(executiveId, assistantId, tenantId).isPresent();
    }

    private String resolveDisplayName(String userId) {
        return userId;
    }

}
