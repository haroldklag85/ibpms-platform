package com.ibpms.poc.infrastructure.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ibpms_pt_milestone")
public class ProjectMilestoneEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "phase_id", nullable = false)
    private ProjectPhaseEntity phase;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "milestone_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "milestone", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTaskEntity> tasks = new ArrayList<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProjectPhaseEntity getPhase() {
        return phase;
    }

    public void setPhase(ProjectPhaseEntity phase) {
        this.phase = phase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<ProjectTaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<ProjectTaskEntity> tasks) {
        this.tasks = tasks;
    }
}
