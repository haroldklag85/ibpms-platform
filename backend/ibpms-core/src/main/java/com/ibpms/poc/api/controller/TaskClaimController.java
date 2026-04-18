package com.ibpms.poc.api.controller;

import com.ibpms.poc.infrastructure.websocket.WorkdeskNotificationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*") // Permisivo en E2E temporal
public class TaskClaimController {

    private final StringRedisTemplate redisTemplate;
    private final WorkdeskNotificationService notificationService;
    private final com.ibpms.poc.application.service.AgileTaskService taskService;

    public TaskClaimController(StringRedisTemplate redisTemplate, WorkdeskNotificationService notificationService, com.ibpms.poc.application.service.AgileTaskService taskService) {
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
        this.taskService = taskService;
    }

    /**
     * POST /api/v1/tasks/{taskId}/claim
     * Reclama una tarea basándose en DB como fuente de verdad (SKIP LOCKED).
     */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<?> claimTask(@PathVariable String taskId) {
        String assignee = com.ibpms.poc.application.util.SecurityContextUtils.getAssignee();
        String tenantId = com.ibpms.poc.application.util.SecurityContextUtils.getTenantId();

        try {
            // Delega en el servicio que usa findByIdForUpdate (CA-11) y guarda en DB con JPA (CA-1)
            taskService.claimTask(java.util.UUID.fromString(taskId), assignee);
            
            // Notificar eventos de WebSocket a los demás (US-001)
            notificationService.notifyTaskClaimed(tenantId, taskId, assignee);

            return ResponseEntity.ok(Map.of("message", "Tarea reclamada exitosamente.", "taskId", taskId));
        } catch (org.springframework.web.server.ResponseStatusException | org.springframework.dao.OptimisticLockingFailureException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "CONFLICT", "message", "La tarea ya fue reclamada por alguien más."));
        }
    }

    /**
     * POST /api/v1/tasks/{taskId}/unclaim
     * Libera voluntariamente una tarea.
     */
    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<?> unclaimTask(@PathVariable String taskId) {
        String assignee = com.ibpms.poc.application.util.SecurityContextUtils.getAssignee();
        String tenantId = com.ibpms.poc.application.util.SecurityContextUtils.getTenantId();
        
        try {
            taskService.unclaimTask(java.util.UUID.fromString(taskId), assignee);
            
            notificationService.notifyTaskUnclaimed(tenantId, taskId);

            return ResponseEntity.ok(Map.of("message", "Tarea liberada."));
        } catch (Exception ex) {
             return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "CONFLICT", "message", "No se puede liberar la tarea."));
        }
    }
}

// Clase de utilidad para el mapa
class Map {
    public static java.util.Map<String, String> of(String k1, String v1) {
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put(k1, v1);
        return m;
    }
    public static java.util.Map<String, String> of(String k1, String v1, String k2, String v2) {
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
