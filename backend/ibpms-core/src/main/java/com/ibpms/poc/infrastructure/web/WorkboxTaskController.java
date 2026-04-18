package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.application.service.AgileTaskService;
import com.ibpms.poc.application.service.TaskDraftService;
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

    public WorkboxTaskController(AgileTaskService taskService, TaskDraftService draftService) {
        this.taskService = taskService;
        this.draftService = draftService;
    }

    /**
     * US-002: Reclamar tarea (asume propiedad exclusiva).
     */
    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> claimTask(@PathVariable UUID id, Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        taskService.claimTask(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002 CA-28: claim-next. Toma la tarea más alta del pool en atomicidad.
     */
    @PostMapping("/claim-next")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<com.ibpms.poc.domain.model.agile.AgileTask> claimNextTask(Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        com.ibpms.poc.domain.model.agile.AgileTask task = taskService.claimNextTask(username);
        return ResponseEntity.ok(task);
    }

    /**
     * US-002 CA-21: Rollback Optimistic UI ante fallo asíncrono.
     */
    @PostMapping("/{id}/rollback-claim")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> rollbackClaim(@PathVariable UUID id, Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        taskService.rollbackClaim(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-002: Liberar tarea.
     */
    @PostMapping("/{id}/unclaim")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> unclaimTask(@PathVariable UUID id, Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        taskService.unclaimTask(id, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Guardado progresivo (Borrador) con Debounce Server-Side.
     */
    @PutMapping("/{id}/draft")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> saveDraft(@PathVariable UUID id, 
                                          @RequestBody Map<String, Object> payload, 
                                          Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        draftService.saveDraft(id, payload, username);
        return ResponseEntity.ok().build();
    }

    /**
     * US-029: Completitud de tarea (Validada).
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> completeTask(@PathVariable UUID id, 
                                             @RequestBody Map<String, Object> payload, 
                                             Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        draftService.completeTask(id, payload, username);
        return ResponseEntity.ok().build();
    }
}
