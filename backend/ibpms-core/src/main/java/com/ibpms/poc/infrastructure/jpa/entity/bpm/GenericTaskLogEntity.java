package com.ibpms.poc.infrastructure.jpa.entity.bpm;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ibpms_generic_task_log")
public class GenericTaskLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(name = "process_instance_id", nullable = false, length = 64)
    private String processInstanceId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "has_evidence", nullable = false)
    private Boolean hasEvidence = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GenericTaskLogEntity() {}

    public GenericTaskLogEntity(String taskId, String processInstanceId, String userId, String comments, Boolean hasEvidence) {
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
        this.userId = userId;
        this.comments = comments;
        this.hasEvidence = hasEvidence;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public Boolean getHasEvidence() { return hasEvidence; }
    public void setHasEvidence(Boolean hasEvidence) { this.hasEvidence = hasEvidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
