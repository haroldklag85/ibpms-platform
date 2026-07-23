// @Traceability: US-003 - ADR-001
package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.AuditLogPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Traceability: US-038, CA-17 (Traza Indeleble de operaciones del sistema)
 * Adapter que persiste eventos de auditoría en ibpms_system_audit_log.
 *
 * CRÍTICO: Se usa REQUIRES_NEW para que la auditoría corra en su PROPIA transacción
 * y conexión de BD. Si falla, NO contamina el estado de la transacción principal
 * (evita PostgreSQL SQLState 25P02 - transaction aborted propagation).
 *
 * Tabla confirmada: ibpms_system_audit_log (migration sprint3/006)
 * Columnas: id VARCHAR(36), user_id VARCHAR(255), action VARCHAR(255),
 *           timestamp TIMESTAMP, message_count INT
 */
@Component
public class AuditLogJdbcAdapter implements AuditLogPort {

    private static final Logger log = LoggerFactory.getLogger(AuditLogJdbcAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public AuditLogJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(String id, String entityType, String entityId, String eventType, String performedBy,
                             LocalDateTime createdAt, byte[] payloadSnapshot, boolean isCompressed,
                             boolean truncated, String detailsJson) {
        try {
            // Componer acción descriptiva con el contexto del evento
            String action = eventType + " | entity=" + entityType + " | id=" + entityId;

            jdbcTemplate.update(
                    "INSERT INTO ibpms_system_audit_log (id, user_id, action, timestamp, message_count) " +
                    "VALUES (?, ?, ?, ?, ?)",
                    id,
                    performedBy,
                    action,
                    java.sql.Timestamp.valueOf(createdAt),
                    0
            );
        } catch (Exception ex) {
            // Fail-Safe: La operación de negocio NO se cancela por un fallo de auditoría.
            // La transacción REQUIRES_NEW se revierte sola sin afectar la transacción principal.
            log.error("[AUDIT-FAIL-SAFE] No se pudo registrar evento id={} event={} entity={}: {}",
                    id, eventType, entityType, ex.getMessage());
        }
    }
}
