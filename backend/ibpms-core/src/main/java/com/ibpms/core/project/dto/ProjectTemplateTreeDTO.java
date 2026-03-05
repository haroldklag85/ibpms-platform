package com.ibpms.core.project.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectTemplateTreeDTO {
    private String id; // Si viene vacío, es una nueva plantilla.
    private String name;
    private String description;

    private List<PhaseDTO> phases = new ArrayList<>();
    private List<DependencyDTO> dependencies = new ArrayList<>();

    @Data
    public static class PhaseDTO {
        private String id;
        private String name;
        private Integer orderIndex;
        private List<MilestoneDTO> milestones = new ArrayList<>();
    }

    @Data
    public static class MilestoneDTO {
        private String id;
        private String name;
        private Integer orderIndex;
        private List<TaskDTO> tasks = new ArrayList<>();
    }

    @Data
    public static class TaskDTO {
        private String id;
        private String name;
        private String description;
        private Integer estimatedHours;
        private String formKey;
        private Integer orderIndex;
    }

    @Data
    public static class DependencyDTO {
        private String sourceTaskId;
        private String targetTaskId;
        private String dependencyType;
        private Integer lagHours;
    }
}
