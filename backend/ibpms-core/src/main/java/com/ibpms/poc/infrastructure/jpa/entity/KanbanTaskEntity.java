package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityListeners;
import com.ibpms.poc.infrastructure.event.KanbanTaskSyncListener;

@Entity
@Table(name = "ibpms_task")
@EntityListeners(KanbanTaskSyncListener.class)
public class KanbanTaskEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private KanbanBoardEntity board;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "assignee", length = 100)
    private String assignee;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "sla_due_date")
    private LocalDateTime slaDueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", referencedColumnName = "id")
    private KanbanTaskEntity parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL)
    private List<KanbanTaskEntity> subTasks = new ArrayList<>();

    public KanbanTaskEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.status = "TODO";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public KanbanBoardEntity getBoard() {
        return board;
    }

    public void setBoard(KanbanBoardEntity board) {
        this.board = board;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(LocalDateTime slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public KanbanTaskEntity getParentTask() {
        return parentTask;
    }

    public void setParentTask(KanbanTaskEntity parentTask) {
        this.parentTask = parentTask;
    }

    public List<KanbanTaskEntity> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<KanbanTaskEntity> subTasks) {
        this.subTasks = subTasks;
    }
}
