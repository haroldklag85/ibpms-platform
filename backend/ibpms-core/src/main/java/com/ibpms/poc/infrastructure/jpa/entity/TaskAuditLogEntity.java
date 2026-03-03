package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_task_audit_log")
public class TaskAuditLogEntity {

    @Id
    private UUID id;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(name = "action", nullable = false, length = 50)
    private String action; // Ej: "UNCLAIM", "REASSIGN"

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "target_username", length = 100)
    private String targetUsername; // Para REASSIGN (ping-pong)

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public TaskAuditLogEntity() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }

    public TaskAuditLogEntity(String taskId, String action, String username, String targetUsername, String reason) {
        this();
        this.taskId = taskId;
        this.action = action;
        this.username = username;
        this.targetUsername = targetUsername;
        this.reason = reason;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
