package com.ibpms.poc.domain.model.audit;

import java.time.Instant;
import java.util.UUID;

public class ClaimAuditLog {
    
    private UUID id;
    private UUID taskId;
    private String userId;
    private String actionType;
    private String tenantId;
    private Instant timestamp;
    
    private String previousAssignee;
    private String reason;
    private String message;

    public ClaimAuditLog() {}

    public ClaimAuditLog(UUID taskId, String userId, String actionType, String tenantId, Instant timestamp, String previousAssignee, String reason, String message) {
        this.taskId = taskId;
        this.userId = userId;
        this.actionType = actionType;
        this.tenantId = tenantId;
        this.timestamp = timestamp;
        this.previousAssignee = previousAssignee;
        this.reason = reason;
        this.message = message;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPreviousAssignee() { return previousAssignee; }
    public void setPreviousAssignee(String previousAssignee) { this.previousAssignee = previousAssignee; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
