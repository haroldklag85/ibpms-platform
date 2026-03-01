package com.ibpms.poc.domain.model;

import java.util.UUID;

/**
 * Entidad de Dominio para una Fase dentro del ProjectTemplate (WBS).
 */
public class PhaseTemplate {

    private UUID id;
    private UUID projectTemplateId; // Referencia al agregado raíz
    private String name;
    private String description;
    private int orderIndex;
    private String defaultAssigneeRole; // Rol o grupo que ejecutará la fase (Ej. "Líder_Técnico")

    public PhaseTemplate(UUID id, String name, String description, int orderIndex, String defaultAssigneeRole) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.orderIndex = orderIndex;
        this.defaultAssigneeRole = defaultAssigneeRole;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjectTemplateId() {
        return projectTemplateId;
    }

    public void setProjectTemplateId(UUID projectTemplateId) {
        this.projectTemplateId = projectTemplateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getDefaultAssigneeRole() {
        return defaultAssigneeRole;
    }

    public void setDefaultAssigneeRole(String defaultAssigneeRole) {
        this.defaultAssigneeRole = defaultAssigneeRole;
    }
}
