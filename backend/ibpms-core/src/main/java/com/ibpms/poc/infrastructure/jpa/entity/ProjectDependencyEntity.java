package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ibpms_pt_dependency")
public class ProjectDependencyEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "source_task_id", nullable = false)
    private ProjectTaskEntity sourceTask;

    @ManyToOne
    @JoinColumn(name = "target_task_id", nullable = false)
    private ProjectTaskEntity targetTask;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProjectTaskEntity getSourceTask() {
        return sourceTask;
    }

    public void setSourceTask(ProjectTaskEntity sourceTask) {
        this.sourceTask = sourceTask;
    }

    public ProjectTaskEntity getTargetTask() {
        return targetTask;
    }

    public void setTargetTask(ProjectTaskEntity targetTask) {
        this.targetTask = targetTask;
    }
}
