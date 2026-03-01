package com.ibpms.poc.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de Dominio para una Plantilla de Proyecto WBS.
 */
public class ProjectTemplate {

    private UUID id;
    private String name;
    private String description;
    private String category;
    private List<PhaseTemplate> phases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public ProjectTemplate(UUID id, String name, String description, String category, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.phases = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.createdBy = createdBy;
    }

    public void addPhase(PhaseTemplate phase) {
        if (this.phases == null) {
            this.phases = new ArrayList<>();
        }
        phase.setProjectTemplateId(this.id);
        this.phases.add(phase);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public List<PhaseTemplate> getPhases() {
        return phases;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPhases(List<PhaseTemplate> phases) {
        this.phases = phases;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
