// @Traceability: US-005, CA-42 - Activity Timeline
package com.ibpms.poc.application.port.out;

import java.util.List;
import java.util.UUID;
import com.ibpms.poc.domain.model.BpmnDesignAuditEntry;

public interface BpmnAuditPort {
    void logAction(UUID processDesignId, String action, String userId, int versionAffected, String details);
    List<BpmnDesignAuditEntry> getAuditLogsForProcess(UUID processDesignId);
}
