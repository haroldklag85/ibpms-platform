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
        private int orderIndex;
        private List<MilestoneDTO> milestones;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }

        public List<MilestoneDTO> getMilestones() {
            return milestones;
        }

        public void setMilestones(List<MilestoneDTO> milestones) {
            this.milestones = milestones;
        }
    }

    public static class MilestoneDTO {
        private String name;
        private int orderIndex;
        private List<TaskDTO> tasks;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }

        public List<TaskDTO> getTasks() {
            return tasks;
        }

        public void setTasks(List<TaskDTO> tasks) {
            this.tasks = tasks;
        }
    }

    public static class TaskDTO {
        private String id; // Required for Dependency referencing
        private String name;
        private int durationDays;
        private String formKey;
        private List<DependencyDTO> dependencies;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDurationDays() {
            return durationDays;
        }

        public void setDurationDays(int durationDays) {
            this.durationDays = durationDays;
        }

        public String getFormKey() {
            return formKey;
        }

        public void setFormKey(String formKey) {
            this.formKey = formKey;
        }

        public List<DependencyDTO> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<DependencyDTO> dependencies) {
            this.dependencies = dependencies;
        }
    }

    public static class DependencyDTO {
        private String sourceTaskId;
        private String targetTaskId;

        public String getSourceTaskId() {
            return sourceTaskId;
        }

        public void setSourceTaskId(String sourceTaskId) {
            this.sourceTaskId = sourceTaskId;
        }

        public String getTargetTaskId() {
            return targetTaskId;
        }

        public void setTargetTaskId(String targetTaskId) {
            this.targetTaskId = targetTaskId;
        }
    }
}
