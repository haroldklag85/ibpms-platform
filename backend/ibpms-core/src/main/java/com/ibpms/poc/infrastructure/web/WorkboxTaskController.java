package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.TaskDraftService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
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
    @Operation(summary = "Reclamar tarea", description = "Asigna una tarea específica al usuario autenticado, marcándola como bloqueada para el resto del equipo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea reclamada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada o ya no disponible"),
        @ApiResponse(responseCode = "409", description = "Conflicto: Tarea ya reclamada por otro usuario")
    })
    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-01"})
    public ResponseEntity<Void> claimTask(@PathVariable UUID id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        taskService.claimTask(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-02: Reclamación Masiva (Bulk Claim).
     * POR QUÉ (Ley Global 3): Implementación requerida para permitir la asignación
     * concurrente e inmutable de múltiples tareas en lotes desde el frontend.
     */
    @Operation(summary = "Reclamo Masivo", description = "Asigna una lista de tareas al usuario actual de manera atómica.")
    @PostMapping("/bulk-claim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-02"})
    public ResponseEntity<Map<String, Object>> bulkClaim(@RequestBody List<String> taskIds) {
        String username = SecurityContextUtils.getAssignee();
        return ResponseEntity.ok(taskService.bulkClaim(taskIds, username));
    }

    /**
     * US-002 CA-28: claim-next. Toma la tarea más alta del pool en atomicidad.
     */
    @Operation(summary = "Atender siguiente (Skill-Based Routing)", description = "Asigna automáticamente la tarea más prioritaria y antigua del pool al usuario actual.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea asignada exitosamente"),
        @ApiResponse(responseCode = "404", description = "No hay tareas disponibles en el pool")
    })
    @PostMapping("/claim-next")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-23"})
    public ResponseEntity<com.ibpms.poc.domain.model.agile.AgileTask> claimNextTask(Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        com.ibpms.poc.domain.model.agile.AgileTask task = taskService.claimNextTask(username);
        return ResponseEntity.ok(task);
    }

    /**
     * US-002 CA-21: Rollback Optimistic UI ante fallo asíncrono.
     */
    @Operation(summary = "Rollback de reclamo", description = "Revierte un reclamo en caso de fallo asíncrono desde la UI.")
    @ApiResponse(responseCode = "200", description = "Rollback exitoso")
    @PostMapping("/{id}/rollback-claim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-21"})
    public ResponseEntity<Void> rollbackClaim(@PathVariable UUID id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        taskService.rollbackClaim(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-04, CA-07: Liberar tarea con motivo.
     * POR QUÉ (Ley Global 3): Se modifica para aceptar un payload opcional que contenga 
     * el "mensajeInterno", posibilitando la auditoría forense del motivo de abandono.
     */
    @Operation(summary = "Liberar tarea", description = "Libera una tarea asignada, opcionalmente con un mensaje de motivo.")
    @PostMapping("/{id}/unclaim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-04", "CA-07"})
    public ResponseEntity<Void> unclaimTask(@PathVariable UUID id, 
                                            @RequestBody(required = false) Map<String, String> payload, 
                                            Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        String mensajeInterno = (payload != null) ? payload.get("mensajeInterno") : null;
        taskService.unclaimTask(id, username, mensajeInterno);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Guardado progresivo (Borrador) con Debounce Server-Side.
     */
    @Operation(summary = "Guardar borrador", description = "Guarda el estado actual del formulario de la tarea sin completarla.")
    @ApiResponse(responseCode = "200", description = "Borrador guardado exitosamente")
    @PutMapping("/{id}/draft")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-029", CA = {"CA-11"})
    public ResponseEntity<Void> saveDraft(@PathVariable UUID id, 
                                          @RequestBody Map<String, Object> payload, 
                                          Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        draftService.saveDraft(id, payload, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Completitud de tarea (Validada).
     * @deprecated Movido a TaskCompletionController por US-017 (CQRS).
     */
    // @PostMapping("/{id}/complete")
    // @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    // @Traceability(US = "US-029", CA = {"CA-01", "CA-16"})
    // public ResponseEntity<Void> completeTask(@PathVariable UUID id, 
    //                                          @RequestBody Map<String, Object> payload, 
    //                                          Authentication auth) {
    //     String username = SecurityContextUtils.getAssignee();
    //     draftService.completeTask(id, payload, username);
    //     return ResponseEntity.ok().build();
    // }

    /**
     * US-002 CA-5: Preview Read-Only sin Lock (No requiere estar asignado).
     * // @Traceability: Retro-Remediación ADR-001 (Hexagonal)
     */
    @Operation(summary = "Previsualizar tarea", description = "Retorna los datos de la tarea en modo solo-lectura, sin realizar un bloqueo (lock).")
    @ApiResponse(responseCode = "200", description = "Datos de la tarea")
    @GetMapping("/{id}/preview")
    @Traceability(US = "US-002", CA = {"CA-05"})
    public ResponseEntity<Map<String, Object>> previewTask(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.previewTask(id));
    }

    /**
     * US-002 CA-8: Force Unclaim de un Supervisor
     * // @Traceability: Retro-Remediación ADR-001 (Hexagonal)
     */
    @Operation(summary = "Forzar liberación (Supervisor)", description = "Permite a un supervisor liberar forzosamente una tarea asignada a otro analista.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea liberada exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol de SUPERVISOR")
    })
    @PostMapping("/{id}/force-unclaim")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-08"})
    public ResponseEntity<Void> forceUnclaim(@PathVariable UUID id) {
        String supervisor = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();
        
        taskService.forceUnclaimTask(id, supervisor, tenantId);

        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-9: Historial de reclamos (Audit Trail).
     */
    @Operation(summary = "Ver historial de auditoría", description = "Retorna el historial de reclamación y liberación de la tarea (Audit Trail).")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    @GetMapping("/{id}/audit-trail")
    @Traceability(US = "US-002", CA = {"CA-09"})
    public ResponseEntity<java.util.List<com.ibpms.poc.domain.model.audit.ClaimAuditLog>> auditTrail(@PathVariable UUID id) {
        return ResponseEntity.ok(claimAuditService.getAuditTrail(id));
    }
}
