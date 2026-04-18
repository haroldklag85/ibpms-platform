package com.ibpms.poc.domain.model.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claim_audit_log")
public class ClaimAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "supervisor_id", nullable = false)
    private String supervisorId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public ClaimAuditLog() {}

    public ClaimAuditLog(UUID taskId, String supervisorId, String actionType, String tenantId, Instant timestamp) {
        this.taskId = taskId;
        this.supervisorId = supervisorId;
        this.actionType = actionType;
        this.tenantId = tenantId;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
