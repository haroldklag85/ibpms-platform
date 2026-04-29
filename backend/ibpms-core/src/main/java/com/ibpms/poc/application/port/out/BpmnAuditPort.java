package com.ibpms.poc.application.port.out;

import java.util.UUID;

public interface BpmnAuditPort {
    void logAction(UUID processDesignId, String action, String userId, int versionAffected, String details);
}
