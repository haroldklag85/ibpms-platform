package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Value Object — Entrada del Git-Log de Diseño BPMN (CA-33).
 */
public class BpmnDesignAuditEntry {

    public enum Action {
        IMPORT, EDIT, SAVE_DRAFT, PRE_FLIGHT, REQUEST_DEPLOY,
        DEPLOY, ROLLBACK, ARCHIVE, LOCK, UNLOCK
    }

    private final UUID id;
    private final UUID processDesignId;
    private final Action action;
    private final String userId;
    private final LocalDateTime timestamp;
    private final int versionAffected;
    private final String details;

    public BpmnDesignAuditEntry(UUID processDesignId, Action action, String userId,
            int versionAffected, String details) {
        this.id = UUID.randomUUID();
        this.processDesignId = processDesignId;
        this.action = action;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.versionAffected = versionAffected;
        this.details = details;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessDesignId() {
        return processDesignId;
    }

    public Action getAction() {
        return action;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getVersionAffected() {
        return versionAffected;
    }

    public String getDetails() {
        return details;
    }
}
