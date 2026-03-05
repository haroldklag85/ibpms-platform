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
@Table(name = "ibpms_pt_task")
public class ProjectTaskEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "milestone_id", nullable = false)
    private ProjectMilestoneEntity milestone;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "role_id")
    private String roleId;

    @Column(name = "form_key")
    private String formKey;

    @OneToMany(mappedBy = "sourceTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectDependencyEntity> outgoingDependencies = new ArrayList<>();

    @OneToMany(mappedBy = "targetTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectDependencyEntity> incomingDependencies = new ArrayList<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProjectMilestoneEntity getMilestone() {
        return milestone;
    }

    public void setMilestone(ProjectMilestoneEntity milestone) {
        this.milestone = milestone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public List<ProjectDependencyEntity> getOutgoingDependencies() {
        return outgoingDependencies;
    }

    public void setOutgoingDependencies(List<ProjectDependencyEntity> outgoingDependencies) {
        this.outgoingDependencies = outgoingDependencies;
    }

    public List<ProjectDependencyEntity> getIncomingDependencies() {
        return incomingDependencies;
    }

    public void setIncomingDependencies(List<ProjectDependencyEntity> incomingDependencies) {
        this.incomingDependencies = incomingDependencies;
    }
}
