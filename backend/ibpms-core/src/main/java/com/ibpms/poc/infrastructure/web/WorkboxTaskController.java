package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.TaskDraftService;
import com.ibpms.poc.application.util.SecurityContextUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.ibpms.poc.crosscutting.annotations.Traceability;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import com.ibpms.poc.infrastructure.jpa.entity.WorkdeskProjectionEntity;

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
    private final com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository projectionRepository;
    private final org.camunda.bpm.engine.TaskService camundaTaskService;

    public WorkboxTaskController(AgileTaskService taskService, TaskDraftService draftService, 
                                 com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService notificationService,
                                 com.ibpms.poc.application.service.ClaimAuditService claimAuditService,
                                 com.ibpms.poc.infrastructure.jpa.repository.WorkdeskProjectionRepository projectionRepository,
                                 org.camunda.bpm.engine.TaskService camundaTaskService) {
        this.taskService = taskService;
        this.draftService = draftService;
        this.notificationService = notificationService;
        this.claimAuditService = claimAuditService;
        this.projectionRepository = projectionRepository;
        this.camundaTaskService = camundaTaskService;
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
    public ResponseEntity<Void> claimTask(@PathVariable String id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        Optional<WorkdeskProjectionEntity> projectionOpt = projectionRepository.findById(id);
        if (projectionOpt.isPresent()) {
            WorkdeskProjectionEntity projection = projectionOpt.get();
            if ("BPMN".equalsIgnoreCase(projection.getSourceSystem())) {
                if (projection.getOriginalTaskId() != null && projection.getOriginalTaskId().startsWith("task_")) {
                    projection.setAssignee(username);
                    projection.setStatus("CLAIMED");
                    projectionRepository.save(projection);
                } else {
                    try {
                        camundaTaskService.claim(projection.getOriginalTaskId(), username);
                    } catch (Exception e) {
                        projection.setAssignee(username);
                        projection.setStatus("CLAIMED");
                        projectionRepository.save(projection);
                    }
                }
            } else {
                taskService.claimTask(UUID.fromString(projection.getOriginalTaskId()), username);
            }
        } else {
            try {
                taskService.claimTask(UUID.fromString(id), username);
            } catch (IllegalArgumentException e) {
                if (id == null || !id.startsWith("task_")) {
                    try {
                        camundaTaskService.claim(id, username);
                    } catch (Exception ex) {}
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-02: Reclamación Masiva (Bulk Claim).
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
    public ResponseEntity<Void> rollbackClaim(@PathVariable String id, Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        Optional<WorkdeskProjectionEntity> projectionOpt = projectionRepository.findById(id);
        if (projectionOpt.isPresent()) {
            WorkdeskProjectionEntity projection = projectionOpt.get();
            if (!"BPMN".equalsIgnoreCase(projection.getSourceSystem())) {
                taskService.rollbackClaim(UUID.fromString(projection.getOriginalTaskId()), username);
            }
        } else {
            try {
                taskService.rollbackClaim(UUID.fromString(id), username);
            } catch (IllegalArgumentException e) {}
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Liberar tarea", description = "Libera una tarea asignada, opcionalmente con un mensaje de motivo.")
    @PostMapping("/{id}/unclaim")
    @PreAuthorize("hasAnyRole('OPERARIO', 'SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-04", "CA-07"})
    public ResponseEntity<Void> unclaimTask(@PathVariable String id, 
                                            @RequestBody(required = false) Map<String, String> payload, 
                                            Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        String mensajeInterno = (payload != null) ? payload.get("mensajeInterno") : null;
        
        Optional<WorkdeskProjectionEntity> projectionOpt = projectionRepository.findById(id);
        if (projectionOpt.isPresent()) {
            WorkdeskProjectionEntity projection = projectionOpt.get();
            if ("BPMN".equalsIgnoreCase(projection.getSourceSystem())) {
                if (projection.getOriginalTaskId() != null && projection.getOriginalTaskId().startsWith("task_")) {
                    projection.setAssignee(null);
                    projection.setStatus("PENDING");
                    projectionRepository.save(projection);
                } else {
                    try {
                        camundaTaskService.claim(projection.getOriginalTaskId(), null);
                    } catch (Exception e) {
                        projection.setAssignee(null);
                        projection.setStatus("PENDING");
                        projectionRepository.save(projection);
                    }
                }
            } else {
                taskService.unclaimTask(UUID.fromString(projection.getOriginalTaskId()), username, mensajeInterno);
            }
        } else {
            try {
                taskService.unclaimTask(UUID.fromString(id), username, mensajeInterno);
            } catch (IllegalArgumentException e) {
                if (id == null || !id.startsWith("task_")) {
                    try {
                        camundaTaskService.claim(id, null);
                    } catch (Exception ex) {}
                }
            }
        }
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
    public ResponseEntity<Void> saveDraft(@PathVariable String id, 
                                          @RequestBody Map<String, Object> payload, 
                                          Authentication auth) {
        String username = SecurityContextUtils.getAssignee();
        Optional<WorkdeskProjectionEntity> projectionOpt = projectionRepository.findById(id);
        if (projectionOpt.isPresent()) {
            WorkdeskProjectionEntity projection = projectionOpt.get();
            if (!"BPMN".equalsIgnoreCase(projection.getSourceSystem())) {
                draftService.saveDraft(UUID.fromString(projection.getOriginalTaskId()), payload, username);
            }
        } else {
            try {
                draftService.saveDraft(UUID.fromString(id), payload, username);
            } catch (IllegalArgumentException e) {}
        }
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-5: Preview Read-Only sin Lock (No requiere estar asignado).
     */
    @Operation(summary = "Previsualizar tarea", description = "Retorna los datos de la tarea en modo solo-lectura, sin realizar un bloqueo (lock).")
    @ApiResponse(responseCode = "200", description = "Datos de la tarea")
    @GetMapping("/{id}/preview")
    @Traceability(US = "US-002", CA = {"CA-05"})
    public ResponseEntity<Map<String, Object>> previewTask(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return ResponseEntity.ok(taskService.previewTask(uuid));
        } catch (IllegalArgumentException e) {
            var projectionOpt = projectionRepository.findById(id);
            if (projectionOpt.isPresent() && !"BPMN".equalsIgnoreCase(projectionOpt.get().getSourceSystem())) {
                try {
                    return ResponseEntity.ok(taskService.previewTask(UUID.fromString(projectionOpt.get().getOriginalTaskId())));
                } catch (Exception ex) {}
            }
            return ResponseEntity.ok(Map.of("id", id, "title", "BPMN Task Preview", "status", "AVAILABLE"));
        }
    }

    /**
     * US-002 CA-8: Force Unclaim de un Supervisor
     */
    @Operation(summary = "Forzar liberación (Supervisor)", description = "Permite a un supervisor liberar forzosamente una tarea asignada a otro analista.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea liberada exitosamente"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado: Se requiere rol de SUPERVISOR")
    })
    @PostMapping("/{id}/force-unclaim")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'SUPER_ADMIN')")
    @Traceability(US = "US-002", CA = {"CA-08"})
    public ResponseEntity<Void> forceUnclaim(@PathVariable String id) {
        String supervisor = SecurityContextUtils.getAssignee();
        String tenantId = SecurityContextUtils.getTenantId();
        
        Optional<WorkdeskProjectionEntity> projectionOpt = projectionRepository.findById(id);
        if (projectionOpt.isPresent()) {
            WorkdeskProjectionEntity projection = projectionOpt.get();
            if ("BPMN".equalsIgnoreCase(projection.getSourceSystem())) {
                if (projection.getOriginalTaskId() != null && projection.getOriginalTaskId().startsWith("task_")) {
                    projection.setAssignee(null);
                    projection.setStatus("PENDING");
                    projectionRepository.save(projection);
                    notificationService.notifyTaskForceUnclaimed(tenantId, projection.getOriginalTaskId());
                } else {
                    try {
                        camundaTaskService.claim(projection.getOriginalTaskId(), null);
                        notificationService.notifyTaskForceUnclaimed(tenantId, projection.getOriginalTaskId());
                    } catch (Exception e) {
                        projection.setAssignee(null);
                        projection.setStatus("PENDING");
                        projectionRepository.save(projection);
                        notificationService.notifyTaskForceUnclaimed(tenantId, projection.getOriginalTaskId());
                    }
                }
            } else {
                taskService.forceUnclaimTask(UUID.fromString(projection.getOriginalTaskId()), supervisor, tenantId);
            }
        } else {
            try {
                taskService.forceUnclaimTask(UUID.fromString(id), supervisor, tenantId);
            } catch (IllegalArgumentException e) {
                if (id == null || !id.startsWith("task_")) {
                    try {
                        camundaTaskService.claim(id, null);
                        notificationService.notifyTaskForceUnclaimed(tenantId, id);
                    } catch (Exception ex) {}
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-9: Historial de reclamos (Audit Trail).
     */
    @Operation(summary = "Ver historial de auditoría", description = "Retorna el historial de reclamación y liberación de la tarea (Audit Trail).")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    @GetMapping("/{id}/audit-trail")
    @Traceability(US = "US-002", CA = {"CA-09"})
    public ResponseEntity<java.util.List<com.ibpms.poc.domain.model.audit.ClaimAuditLog>> auditTrail(@PathVariable String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return ResponseEntity.ok(claimAuditService.getAuditTrail(uuid));
        } catch (IllegalArgumentException e) {
            var projectionOpt = projectionRepository.findById(id);
            if (projectionOpt.isPresent()) {
                try {
                    return ResponseEntity.ok(claimAuditService.getAuditTrail(UUID.fromString(projectionOpt.get().getOriginalTaskId())));
                } catch (Exception ex) {}
            }
            return ResponseEntity.ok(List.of());
        }
    }
}
