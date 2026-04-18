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

    public TaskClaimController(StringRedisTemplate redisTemplate, WorkdeskNotificationService notificationService) {
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    /**
     * POST /api/v1/tasks/{taskId}/claim
     * Reclama una tarea para el usuario autenticado, previniendo condiciones de carrera con Redis SETNX.
     */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<?> claimTask(@PathVariable String taskId) {
        // En producción el assignee y tenantId vienen del JWT Mock (SecurityContext)
        // Para mock E2E usaremos valores hardcodeados temporales indicados por el Auth Bypass helper
        String assignee = "e2e_user"; 
        String tenantId = "tenant_1";

        String lockKey = "lock:task:claim:" + taskId;

        // Intentar adquirir el candado atómico (SETNX) por 2 horas maximo
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, assignee, Duration.ofHours(2));

        if (Boolean.TRUE.equals(lockAcquired)) {
            // [AQUÍ IRÍA LA INYECCIÓN HACIA EL REPOSITORIO DE BASE DE DATOS PARA UPDATES]
            // taskRepository.assignTask(taskId, assignee);

            // Notificar eventos de WebSocket a los demás (US-001)
            notificationService.notifyTaskClaimed(tenantId, taskId, assignee);

            return ResponseEntity.ok(Map.of("message", "Tarea reclamada exitosamente.", "taskId", taskId));
        } else {
            // Falso significa que la llave ya existe (Race condition ganada por otro)
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
        String tenantId = "tenant_1";
        String lockKey = "lock:task:claim:" + taskId;

        redisTemplate.delete(lockKey);

        // [AQUÍ IRÍA ACTUALIZACIÓN A LA BD PONIENDO ASSIGNEE A NULL]
        
        notificationService.notifyTaskUnclaimed(tenantId, taskId);

        return ResponseEntity.ok(Map.of("message", "Tarea liberada."));
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
