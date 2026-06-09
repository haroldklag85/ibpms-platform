package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ibpms_bpmn_design_audit_log")
@IdClass(BpmnDesignAuditLogId.class)
public class BpmnDesignAuditLogEntity {

    public enum Action {
        IMPORT, EDIT, SAVE_DRAFT, PRE_FLIGHT, REQUEST_DEPLOY,
        DEPLOY, ROLLBACK, ARCHIVE, LOCK, UNLOCK
    }

    @Id
    private UUID id;

    @Column(name = "process_design_id", nullable = false)
    private UUID processDesignId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Action action;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Id
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "version_affected", nullable = false)
    private int versionAffected;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    public BpmnDesignAuditLogEntity() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    public BpmnDesignAuditLogEntity(UUID processDesignId, Action action, String userId,
            int versionAffected, Map<String, Object> details) {
        this();
        this.processDesignId = processDesignId;
        this.action = action;
        this.userId = userId;
        this.versionAffected = versionAffected;
        this.details = details;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProcessDesignId() {
        return processDesignId;
    }

    public void setProcessDesignId(UUID processDesignId) {
        this.processDesignId = processDesignId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getVersionAffected() {
        return versionAffected;
    }

    public void setVersionAffected(int versionAffected) {
        this.versionAffected = versionAffected;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
