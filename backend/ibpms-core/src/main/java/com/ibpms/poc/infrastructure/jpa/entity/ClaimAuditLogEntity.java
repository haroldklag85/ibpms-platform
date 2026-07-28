package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_audit_log")
public class ClaimAuditLogEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "supervisor_id", nullable = false)
    private String userId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "previous_assignee")
    private String previousAssignee;

    @Column(name = "reason")
    private String reason;

    @Column(name = "message")
    private String message;

    public ClaimAuditLogEntity() {}

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
