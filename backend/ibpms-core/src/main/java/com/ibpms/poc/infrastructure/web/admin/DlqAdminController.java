package com.ibpms.poc.infrastructure.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/queues/dlq")
@Tag(name = "DLQ Admin", description = "Operaciones administrativas para gestión de la Dead Letter Queue")
public class DlqAdminController {

    private static final Logger log = LoggerFactory.getLogger(DlqAdminController.class);
    private final RabbitAdmin rabbitAdmin;

    public DlqAdminController(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener estado de la cola global DLQ")
    public ResponseEntity<Map<String, Object>> getDlqSummary() {
        Map<String, Object> summary = new HashMap<>();
        try {
            var properties = rabbitAdmin.getQueueProperties("ibpms.dlq.global");
            if (properties != null) {
                summary.put("message_count", properties.get("QUEUE_MESSAGE_COUNT"));
                summary.put("status", "ACTIVE");
            } else {
                summary.put("status", "NOT_FOUND");
            }
        } catch (Exception e) {
            summary.put("status", "ERROR");
            summary.put("error", e.getMessage());
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/retry")
    @Operation(summary = "Reencolar mensajes fallidos (Dummy placeholder)")
    public ResponseEntity<String> retryMessages() {
        // En implementación real, se lee el DLQ, se parsea el 'x-death' header y se enruta de vuelta al original.
        log.warn("SUDO INVOKE: Ejecución de reintentos masivos de la DLQ.");
        return ResponseEntity.ok("Requeue process triggered.");
    }

    @DeleteMapping("/purge")
    @Operation(summary = "Purgar la DLQ central")
    public ResponseEntity<String> purgeDlq() {
        log.warn("SUDO INVOKE: Purgando totalmente ibpms.dlq.global. Datos no archivables perdidos permanentemente.");
        rabbitAdmin.purgeQueue("ibpms.dlq.global", false);
        return ResponseEntity.ok("DLQ Purged.");
    }
}
