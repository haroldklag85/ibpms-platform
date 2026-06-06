package com.ibpms.poc.application.port.out;

import java.time.LocalDateTime;

public interface AuditLogPort {
    void saveAuditLog(String id, String entityType, String entityId, String eventType, String performedBy, LocalDateTime createdAt, byte[] payloadSnapshot, boolean isCompressed, boolean truncated, String detailsJson);
}
