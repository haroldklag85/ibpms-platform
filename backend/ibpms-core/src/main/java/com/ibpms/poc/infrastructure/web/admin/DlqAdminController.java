package com.ibpms.poc.infrastructure.web.admin;

import com.ibpms.poc.infrastructure.jpa.entity.SystemAuditLogEntity;
import com.ibpms.poc.infrastructure.jpa.repository.SystemAuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/admin/queues/dlq")
@Tag(name = "DLQ Admin", description = "Operaciones administrativas para gestión de la Dead Letter Queue")
@PreAuthorize("hasRole('ADMIN_IT')")
public class DlqAdminController {

    private static final Logger log = LoggerFactory.getLogger(DlqAdminController.class);
    private final RabbitAdmin rabbitAdmin;
    private final SystemAuditLogRepository auditRepository;

    public DlqAdminController(RabbitAdmin rabbitAdmin, SystemAuditLogRepository auditRepository) {
        this.rabbitAdmin = rabbitAdmin;
        this.auditRepository = auditRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener estado de la cola global DLQ")
    public ResponseEntity<Map<String, Object>> getDlqSummary() {
        Map<String, Object> summary = new HashMap<>();
        try {
            Properties properties = rabbitAdmin.getQueueProperties("ibpms.dlq.global");
            if (properties != null) {
                summary.put("message_count", properties.get("QUEUE_MESSAGE_COUNT"));
                summary.put("status", "ACTIVE");
            }
        } catch (Exception e) {
            summary.put("status", "ERROR");
            summary.put("error", e.getMessage());
        }
        return ResponseEntity.ok(summary);
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.getName() != null) ? authentication.getName() : "SYSTEM";
    }

    private Integer getQueueCount() {
        try {
            Properties properties = rabbitAdmin.getQueueProperties("ibpms.dlq.global");
            if (properties != null && properties.get("QUEUE_MESSAGE_COUNT") != null) {
                return Integer.parseInt(properties.get("QUEUE_MESSAGE_COUNT").toString());
            }
        } catch(Exception ignored) {}
        return 0;
    }

    @PostMapping("/retry")
    @Operation(summary = "Reencolar mensajes fallidos (Dummy placeholder)")
    public ResponseEntity<String> retryMessages() {
        String actor = getCurrentUser();
        Integer count = getQueueCount();
        
        // CA-8: Rastro Forense Persistido
        auditRepository.save(new SystemAuditLogEntity(actor, "Retry", count));
        
        log.warn("SUDO INVOKE [Audit Trail]: Ejecución de reintentos masivos de la DLQ solicitada por usuario: {}", actor);
        return ResponseEntity.ok("Requeue process triggered.");
    }

    @DeleteMapping("/purge")
    @Operation(summary = "Purgar la DLQ central")
    public ResponseEntity<String> purgeDlq() {
        String actor = getCurrentUser();
        Integer count = getQueueCount();
        
        // CA-8: Rastro Forense Persistido
        auditRepository.save(new SystemAuditLogEntity(actor, "Purge", count));
        
        log.warn("SUDO INVOKE [Audit Trail]: Purgando totalmente ibpms.dlq.global. Datos no archivables perdidos permanentemente. Acción ejecutada por usuario: {}", actor);
        rabbitAdmin.purgeQueue("ibpms.dlq.global", false);
        return ResponseEntity.ok("DLQ Purged.");
    }
}
