package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.AuditLogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Traceability: US-038, CA-17 (Traza Indeleble de operaciones del sistema)
 * Adapter que persiste eventos de auditoría en ibpms_security_audit_log.
 * NOTA: Se usa ibpms_security_audit_log (garantizadamente existente desde migración 20)
 * en lugar de ibpms_audit_log para evitar dependencias en migraciones no aplicadas.
 */
@Component
public class AuditLogJdbcAdapter implements AuditLogPort {

    private static final Logger log = LoggerFactory.getLogger(AuditLogJdbcAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditLogJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAuditLog(String id, String entityType, String entityId, String eventType, String performedBy,
                             LocalDateTime createdAt, byte[] payloadSnapshot, boolean isCompressed,
                             boolean truncated, String detailsJson) {
        try {
            // Componer mensaje de auditoría legible para el campo action
            String action = eventType + " | entity=" + entityType + " | id=" + entityId;
            String endpoint = "SYSTEM::" + eventType;

            jdbcTemplate.update(
                    "INSERT INTO ibpms_security_audit_log " +
                    "(id, user_id, action, message_count, endpoint_invocado, timestamp_utc) " +
                    "VALUES (?::uuid, ?, ?, ?, ?, ?)",
                    UUID.fromString(id),
                    performedBy,
                    action,
                    0,
                    endpoint,
                    java.sql.Timestamp.valueOf(createdAt)
            );
        } catch (Exception ex) {
            // CA-17: Fail-Safe de Auditoría. La operación de negocio NO se cancela por un fallo de log.
            log.error("[AUDIT-FAIL-SAFE] No se pudo registrar evento de auditoría id={} event={} entity={}: {}",
                    id, eventType, entityType, ex.getMessage());
        }
    }
}
