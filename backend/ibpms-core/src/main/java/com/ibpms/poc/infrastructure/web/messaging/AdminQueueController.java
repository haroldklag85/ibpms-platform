package com.ibpms.poc.infrastructure.web.messaging;

import com.ibpms.poc.application.service.messaging.DlqManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST del Dashboard técnico de Dead Letter Queue (DLQ).
 * Expone endpoints para monitoreo, reintento y purgado de la DLQ global.
 * <p>
 * Protegido exclusivamente para los roles ADMIN_IT y SUPER_ADMIN.
 * <p>
 * @Traceability(US = "US-034", CA = "CA-02")
 */
@RestController
@RequestMapping("/api/v1/admin/queues/dlq")
@PreAuthorize("hasAnyRole('ADMIN_IT', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminQueueController {

    private static final Logger log = LoggerFactory.getLogger(AdminQueueController.class);

    private final DlqManagementService dlqManagementService;

    public AdminQueueController(DlqManagementService dlqManagementService) {
        this.dlqManagementService = dlqManagementService;
    }

    /**
     * Retorna un resumen del estado actual de la DLQ global.
     * Incluye el conteo total de mensajes y los más antiguos.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        log.info("[DLQ-DASHBOARD] Solicitud de resumen DLQ.");
        Map<String, Object> summary = dlqManagementService.getDlqSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Reintenta el procesamiento de mensajes en la DLQ.
     *
     * @param maxMessages Máximo de mensajes a reintentar (por defecto 10).
     */
    @PostMapping("/retry")
    public ResponseEntity<Map<String, Object>> retryMessages(
            @RequestParam(defaultValue = "10") int maxMessages) {
        log.info("[DLQ-DASHBOARD] Solicitud de retry. maxMessages={}", maxMessages);
        int retried = dlqManagementService.retryMessages(maxMessages);
        return ResponseEntity.ok(Map.of("retriedCount", retried));
    }

    /**
     * Purga completamente la DLQ global. Acción destructiva e irreversible.
     * Requiere confirmación explícita del operador con rol ADMIN_IT.
     */
    @DeleteMapping("/purge")
    @PreAuthorize("hasAnyRole('ADMIN_IT', 'SUPER_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> purge() {
        log.warn("[DLQ-DASHBOARD] ⚠️ Solicitud de PURGA de DLQ global.");
        dlqManagementService.purge();
        return ResponseEntity.ok(Map.of("status", "PURGED", "queue", "ibpms.dlq.global"));
    }
}
