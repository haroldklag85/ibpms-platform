package com.ibpms.poc.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProjectTemplateDTO {

    private UUID id;
    private String name;
    private String description;
    private String category;
    private List<PhaseDTO> phases;
    private LocalDateTime createdAt;
    private String createdBy;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<PhaseDTO> getPhases() {
        return phases;
    }

    public void setPhases(List<PhaseDTO> phases) {
        this.phases = phases;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public static class PhaseDTO {
        private String name;
        private String description;
        private int orderIndex;
        private String defaultAssigneeRole;

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
}
