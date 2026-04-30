package com.ibpms.poc.infrastructure.adapter;

import com.ibpms.poc.application.port.out.AuditLogPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditLogJdbcAdapter implements AuditLogPort {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAuditLog(String id, String entityType, String entityId, String eventType, String performedBy, LocalDateTime createdAt, byte[] payloadSnapshot, boolean isCompressed, boolean truncated, String detailsJson) {
        jdbcTemplate.update(
                "INSERT INTO ibpms_audit_log (id, entity_type, entity_id, event_type, performed_by, created_at, payload_snapshot, is_compressed, truncated, details) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)",
                id, entityType, entityId, eventType, performedBy,
                java.sql.Timestamp.valueOf(createdAt),
                payloadSnapshot, isCompressed, truncated, detailsJson
        );
    }
}
