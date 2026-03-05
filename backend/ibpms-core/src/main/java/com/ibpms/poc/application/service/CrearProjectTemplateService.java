package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.ProjectTemplateDTO;
import com.ibpms.poc.application.port.in.CrearProjectTemplateUseCase;
import com.ibpms.poc.infrastructure.jpa.entity.ProjectTemplateEntity;
import com.ibpms.poc.infrastructure.jpa.repository.ProjectTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CrearProjectTemplateService implements CrearProjectTemplateUseCase {

    private final ProjectTemplateRepository repository;

    public CrearProjectTemplateService(ProjectTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProjectTemplateDTO crearPlantilla(ProjectTemplateDTO dto, String createdBy) {
        // Enforce Topological Sort (Cycle Detection)
        if (hasCyclicDependencies(dto)) {
            throw new IllegalArgumentException(
                    "Cyclic Dependencies Detected (HTTP 400). A project task cannot depend on itself or back-track.");
        }

        ProjectTemplateEntity entity = new ProjectTemplateEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setCreatedBy(createdBy);

        java.util.Map<String, com.ibpms.poc.infrastructure.jpa.entity.ProjectTaskEntity> taskRegistry = new java.util.HashMap<>();

        if (dto.getPhases() != null) {
            for (ProjectTemplateDTO.PhaseDTO phaseDto : dto.getPhases()) {
                com.ibpms.poc.infrastructure.jpa.entity.ProjectPhaseEntity phaseEntity = new com.ibpms.poc.infrastructure.jpa.entity.ProjectPhaseEntity();
                phaseEntity.setId(UUID.randomUUID());
                phaseEntity.setName(phaseDto.getName());
                phaseEntity.setOrder(phaseDto.getOrderIndex());
                phaseEntity.setTemplate(entity);
                entity.getPhases().add(phaseEntity);

                if (phaseDto.getMilestones() != null) {
                    for (ProjectTemplateDTO.MilestoneDTO milestoneDto : phaseDto.getMilestones()) {
                        com.ibpms.poc.infrastructure.jpa.entity.ProjectMilestoneEntity milestoneEntity = new com.ibpms.poc.infrastructure.jpa.entity.ProjectMilestoneEntity();
                        milestoneEntity.setId(UUID.randomUUID());
                        milestoneEntity.setName(milestoneDto.getName());
                        milestoneEntity.setOrder(milestoneDto.getOrderIndex());
                        milestoneEntity.setPhase(phaseEntity);
                        phaseEntity.getMilestones().add(milestoneEntity);

                        if (milestoneDto.getTasks() != null) {
                            for (ProjectTemplateDTO.TaskDTO taskDto : milestoneDto.getTasks()) {
                                com.ibpms.poc.infrastructure.jpa.entity.ProjectTaskEntity taskEntity = new com.ibpms.poc.infrastructure.jpa.entity.ProjectTaskEntity();
                                taskEntity.setId(UUID.randomUUID());
                                taskEntity.setName(taskDto.getName());
                                taskEntity.setDurationDays(
                                        taskDto.getDurationDays() != 0 ? taskDto.getDurationDays() : 1);
                                taskEntity.setFormKey(taskDto.getFormKey());
                                taskEntity.setMilestone(milestoneEntity);
                                milestoneEntity.getTasks().add(taskEntity);
                                taskRegistry.put(taskDto.getId(), taskEntity); // Map frontend pseudo-UUID to real DB
                                                                               // UUID Object
                            }
                        }
                    }
                }
            }
        }

        // Map Dependencies now that all tasks exist in the registry
        if (dto.getPhases() != null) {
            for (ProjectTemplateDTO.PhaseDTO p : dto.getPhases()) {
                if (p.getMilestones() != null) {
                    for (ProjectTemplateDTO.MilestoneDTO m : p.getMilestones()) {
                        if (m.getTasks() != null) {
                            for (ProjectTemplateDTO.TaskDTO t : m.getTasks()) {
                                if (t.getDependencies() != null) {
                                    for (ProjectTemplateDTO.DependencyDTO dep : t.getDependencies()) {
                                        com.ibpms.poc.infrastructure.jpa.entity.ProjectTaskEntity source = taskRegistry
                                                .get(dep.getSourceTaskId());
                                        com.ibpms.poc.infrastructure.jpa.entity.ProjectTaskEntity target = taskRegistry
                                                .get(dep.getTargetTaskId());

                                        if (source != null && target != null) {
                                            com.ibpms.poc.infrastructure.jpa.entity.ProjectDependencyEntity depEntity = new com.ibpms.poc.infrastructure.jpa.entity.ProjectDependencyEntity();
                                            depEntity.setId(UUID.randomUUID());
                                            depEntity.setSourceTask(source);
                                            depEntity.setTargetTask(target);
                                            source.getOutgoingDependencies().add(depEntity);
                                            target.getIncomingDependencies().add(depEntity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        repository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    private boolean hasCyclicDependencies(ProjectTemplateDTO dto) {
        java.util.Map<String, java.util.List<String>> graph = new java.util.HashMap<>();

        // Build Directed Graph
        if (dto.getPhases() != null) {
            for (ProjectTemplateDTO.PhaseDTO p : dto.getPhases()) {
                if (p.getMilestones() != null) {
                    for (ProjectTemplateDTO.MilestoneDTO m : p.getMilestones()) {
                        if (m.getTasks() != null) {
                            for (ProjectTemplateDTO.TaskDTO t : m.getTasks()) {
                                graph.putIfAbsent(t.getId(), new java.util.ArrayList<>());
                                if (t.getDependencies() != null) {
                                    for (ProjectTemplateDTO.DependencyDTO d : t.getDependencies()) {
                                        graph.get(t.getId()).add(d.getTargetTaskId()); // Note: Task UI structure
                                                                                       // implies t is Source
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Set<String> recursionStack = new java.util.HashSet<>();

        for (String node : graph.keySet()) {
            if (dfsCycleDetect(node, graph, visited, recursionStack)) {
                return true; // Cycle Found
            }
        }
        return false;
    }

    private boolean dfsCycleDetect(String node, java.util.Map<String, java.util.List<String>> graph,
            java.util.Set<String> visited, java.util.Set<String> recursionStack) {
        if (recursionStack.contains(node))
            return true;
        if (visited.contains(node))
            return false;

        visited.add(node);
        recursionStack.add(node);

        java.util.List<String> neighbors = graph.getOrDefault(node, new java.util.ArrayList<>());
        for (String neighbor : neighbors) {
            if (dfsCycleDetect(neighbor, graph, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }
}
