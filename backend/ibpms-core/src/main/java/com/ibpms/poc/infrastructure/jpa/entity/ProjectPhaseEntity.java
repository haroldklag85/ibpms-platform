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
@Table(name = "ibpms_pt_phase")
public class ProjectPhaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private ProjectTemplateEntity template;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phase_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMilestoneEntity> milestones = new ArrayList<>();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ProjectTemplateEntity getTemplate() {
        return template;
    }

    public void setTemplate(ProjectTemplateEntity template) {
        this.template = template;
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

    public List<ProjectMilestoneEntity> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<ProjectMilestoneEntity> milestones) {
        this.milestones = milestones;
    }
}
