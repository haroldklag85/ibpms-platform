package com.ibpms.poc.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BpmnDesignAuditLogDTO {
    private UUID id;
    private String action;
    private String userId;
    private LocalDateTime timestamp;
    private int versionAffected;
    private String details;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
