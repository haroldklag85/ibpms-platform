package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.TaskDraftService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoint Dedicado para las operaciones reactivas y de estado
 * (Borrador, Claim, Unclaim, Complete) del Workbox de Tareas Ágiles.
 * 
 * Separación delegada por Arquitectura: AgileTaskController maneja el CRUD puro,
 * WorkboxTaskController maneja el ciclo de vida del SLA y ejecución.
 */
@RestController
@RequestMapping("/api/v1/workbox/tasks")
public class WorkboxTaskController {

    private final AgileTaskService taskService;
    private final TaskDraftService draftService;
    private final com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService notificationService;
    private final com.ibpms.poc.application.service.ClaimAuditService claimAuditService;

    public WorkboxTaskController(AgileTaskService taskService, TaskDraftService draftService, 
                                 com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService notificationService,
                                 com.ibpms.poc.application.service.ClaimAuditService claimAuditService) {
        this.taskService = taskService;
        this.draftService = draftService;
        this.notificationService = notificationService;
        this.claimAuditService = claimAuditService;
    }

    /**
     * US-002: Reclamar tarea (asume propiedad exclusiva).
     */
    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> claimTask(@PathVariable UUID id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        taskService.claimTask(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-28: claim-next. Toma la tarea más alta del pool en atomicidad.
     */
    @PostMapping("/claim-next")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<com.ibpms.poc.domain.model.agile.AgileTask> claimNextTask(Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        com.ibpms.poc.domain.model.agile.AgileTask task = taskService.claimNextTask(username);
        return ResponseEntity.ok(task);
    }

    /**
     * US-002 CA-21: Rollback Optimistic UI ante fallo asíncrono.
     */
    @PostMapping("/{id}/rollback-claim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> rollbackClaim(@PathVariable UUID id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        taskService.rollbackClaim(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002: Liberar tarea.
     */
    @PostMapping("/{id}/unclaim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> unclaimTask(@PathVariable UUID id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        taskService.unclaimTask(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Guardado progresivo (Borrador) con Debounce Server-Side.
     */
    @PutMapping("/{id}/draft")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> saveDraft(@PathVariable UUID id, 
                                          @RequestBody Map<String, Object> payload, 
                                          Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        draftService.saveDraft(id, payload, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Completitud de tarea (Validada).
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> completeTask(@PathVariable UUID id, 
                                             @RequestBody Map<String, Object> payload, 
                                             Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        draftService.completeTask(id, payload, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-5: Preview Read-Only sin Lock (No requiere estar asignado).
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Map<String, Object>> previewTask(@PathVariable UUID id) {
        com.ibpms.poc.domain.model.agile.AgileTask task = taskService.getTask(id);
        // Exponer solo datos genéricos sin estados alterables:
        return ResponseEntity.ok(Map.of(
                "title", task.getTitle(),
                "description", task.getDescription(),
                "slaExpiration", task.getSlaDeadline(),
                "status", task.getStatus(),
                "assignee", task.getAssigneeIds() != null && !task.getAssigneeIds().isEmpty() ? String.join(",", task.getAssigneeIds()) : ""
        ));
    }

    /**
     * US-002 CA-8: Force Unclaim de un Supervisor
     */
    @PostMapping("/{id}/force-unclaim")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    public ResponseEntity<Void> forceUnclaim(@PathVariable UUID id) {
        String supervisor = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();
        
        // El taskService libera la tarea igual que un unclaim normal, pero saltando seguridad de current assignee
        taskService.forceUnclaimTask(id);

        // Emitir audit y notificación
        claimAuditService.auditForceUnclaim(id, supervisor, tenantId);
        notificationService.notifyTaskForceUnclaimed(tenantId, id.toString());

        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-9: Historial de reclamos (Audit Trail).
     */
    @GetMapping("/{id}/audit-trail")
    public ResponseEntity<java.util.List<com.ibpms.poc.domain.model.audit.ClaimAuditLog>> auditTrail(@PathVariable UUID id) {
        return ResponseEntity.ok(claimAuditService.getAuditTrail(id));
    }
}
