package com.ibpms.poc.domain.model.kanban;

import java.time.ZonedDateTime;
import java.util.UUID;

public class KanbanTask {
    private UUID id;
    private UUID boardId;
    private String title;
    private String description;
    private KanbanState status;
    private String assignee;
    private String priority;
    private ZonedDateTime slaDueDate;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String blockedReason;

    public KanbanTask(UUID id, UUID boardId, String title, String description, KanbanState status, String assignee, String priority, ZonedDateTime slaDueDate, ZonedDateTime createdAt, ZonedDateTime updatedAt, String blockedReason) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.status = status != null ? status : KanbanState.TODO;
        this.assignee = assignee;
        this.priority = priority;
        this.slaDueDate = slaDueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.blockedReason = blockedReason;
    }

    public KanbanTask() {}

    public boolean isImmutable() {
        return status != null && status.isImmutable();
    }

    public void validateTransition(KanbanState newState) {
        if (isImmutable()) {
            throw new IllegalStateException("Tarea en DONE es inmutable");
        }
        if (this.status != null && !this.status.canTransitionTo(newState)) {
            throw new IllegalStateException("Transición inválida de " + this.status + " a " + newState);
        }
    }

    public void requireBlockedReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Se requiere una razón para bloquear la tarea");
        }
        this.blockedReason = reason;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBoardId() { return boardId; }
    public void setBoardId(UUID boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public KanbanState getStatus() { return status; }
    public void setStatus(KanbanState status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public ZonedDateTime getSlaDueDate() { return slaDueDate; }
    public void setSlaDueDate(ZonedDateTime slaDueDate) { this.slaDueDate = slaDueDate; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
}
