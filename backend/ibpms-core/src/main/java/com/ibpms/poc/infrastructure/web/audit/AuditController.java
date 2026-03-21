package com.ibpms.poc.infrastructure.web.audit;

import com.ibpms.poc.application.usecase.audit.AuditEventUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Adapter-In (Controller) Hexagonal para Telemetría y Compliance ISO.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditEventUseCase auditEventUseCase;

    public AuditController(AuditEventUseCase auditEventUseCase) {
        this.auditEventUseCase = auditEventUseCase;
    }

    /**
     * CA-10: Guardián de Transparencia de Secretos.
     * Retorna HTTP 200 INMEDIATO para no bloquear la UX del Supervisor.
     */
    @PostMapping("/events")
    public ResponseEntity<?> logAuditEvent(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        String ipAddress = request.getRemoteAddr();

        String eventType = payload.get("eventType");
        String resourceId = payload.get("resourceId");
        String reason = payload.get("reason");

        if ("SECRETS_VIEWED".equals(eventType)) {
            // Delega al UseCase (Port-In) la lógica asíncrona inmutable
            auditEventUseCase.registerSecretViewed(userId, resourceId, reason, ipAddress);
        }

        return ResponseEntity.ok(Map.of("status", "Audit Trail Successfully Stored"));
    }
}
