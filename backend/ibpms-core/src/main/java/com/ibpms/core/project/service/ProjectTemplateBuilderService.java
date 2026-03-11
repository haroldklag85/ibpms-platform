package com.ibpms.core.project.service;

import com.ibpms.core.project.domain.*;
import com.ibpms.core.project.dto.ProjectTemplateTreeDTO;
import com.ibpms.core.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProjectTemplateBuilderService {

    private final ProjectTemplateRepository templateRepo;
    private final ProjectTemplateDependencyRepository dependencyRepo;
    private final ProjectTemplateTaskRepository taskRepo;

    public ProjectTemplateBuilderService(ProjectTemplateRepository templateRepo,
            ProjectTemplateDependencyRepository dependencyRepo,
            ProjectTemplateTaskRepository taskRepo) {
        this.templateRepo = templateRepo;
        this.dependencyRepo = dependencyRepo;
        this.taskRepo = taskRepo;
    }

    @Transactional
    public ProjectTemplateTreeDTO deepSaveTemplate(ProjectTemplateTreeDTO dto) {

        // 1. Validacion Topologica (Algoritmo de Kahn)
        // Corta la transaccion con Excepcion (HTTP 400 mapeado en el ExceptionHandler
        // global idealmente) si hay ciclo
        validateAcyclicDependencies(dto.getDependencies());

        ProjectTemplate template;

        // 2. Si ya existe, lo borramos o limpiamos sus dependencias y fases por
        // orfandad
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            template = templateRepo.findById(java.util.Objects.requireNonNull(dto.getId()))
                    .orElse(new ProjectTemplate()); // O lanzar 404

            // Borrado masivo de dependencias viejas para recrearlas
            dependencyRepo.deleteByTemplateId(template.getId());

            // Limpia las fases existentes. Al tener orphanRemoval=true, Hibernate borrara
            // en cascada Hitos y Tareas.
            template.getPhases().clear();
        } else {
            template = new ProjectTemplate();
            template.setCreatedBy("admin_user"); // MOCK JWT V1
        }

        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setStatus("DRAFT");

        // 3. Recrear el Arbol
        for (ProjectTemplateTreeDTO.PhaseDTO phaseDto : dto.getPhases()) {
            ProjectTemplatePhase p = new ProjectTemplatePhase();
            p.setId(phaseDto.getId() != null ? phaseDto.getId() : UUID.randomUUID().toString());
            p.setName(phaseDto.getName());
            p.setOrderIndex(phaseDto.getOrderIndex());
            p.setTemplate(template);

            for (ProjectTemplateTreeDTO.MilestoneDTO msDto : phaseDto.getMilestones()) {
                ProjectTemplateMilestone m = new ProjectTemplateMilestone();
                m.setId(msDto.getId() != null ? msDto.getId() : UUID.randomUUID().toString());
                m.setName(msDto.getName());
                m.setOrderIndex(msDto.getOrderIndex());
                m.setPhase(p);

                for (ProjectTemplateTreeDTO.TaskDTO tDto : msDto.getTasks()) {
                    ProjectTemplateTask t = new ProjectTemplateTask();
                    t.setId(tDto.getId() != null ? tDto.getId() : UUID.randomUUID().toString());
                    t.setName(tDto.getName());
                    t.setDescription(tDto.getDescription());
                    t.setEstimatedHours(tDto.getEstimatedHours());
                    t.setOrderIndex(tDto.getOrderIndex());
                    t.setFormKey(tDto.getFormKey());
                    t.setMilestone(m);

                    m.getTasks().add(t);
                }
                p.getMilestones().add(m);
            }
            template.getPhases().add(p);
        }

        // GUARDADO CASCADA
        ProjectTemplate savedTemplate = templateRepo.saveAndFlush(template);

        // 4. Insercion de Dependencias Nuevas
        for (ProjectTemplateTreeDTO.DependencyDTO dDto : dto.getDependencies()) {
            ProjectTemplateDependency d = new ProjectTemplateDependency();
            d.setTemplateId(savedTemplate.getId());
            d.setSourceTaskId(dDto.getSourceTaskId());
            d.setTargetTaskId(dDto.getTargetTaskId());
            d.setDependencyType(dDto.getDependencyType());
            d.setLagHours(dDto.getLagHours());
            dependencyRepo.save(d);
        }

        dto.setId(savedTemplate.getId());
        return dto;
    }

    @Transactional
    public void publishTemplate(String templateId) {
        ProjectTemplate template = templateRepo.findById(java.util.Objects.requireNonNull(templateId))
                .orElseThrow(() -> new IllegalArgumentException("Template no encontrado"));

        // AC-1: Validacion de Integridad. Todas las tareas deben tener form_key
        List<ProjectTemplateTask> allTasks = taskRepo.findAllTasksByTemplateId(templateId);

        for (ProjectTemplateTask task : allTasks) {
            if (task.getFormKey() == null || task.getFormKey().trim().isEmpty()) {
                // Idealmente lanzar Http 422 UnprocessableEntityException
                throw new IllegalStateException("Integrity Error: La tarea '" + task.getName() + "' (ID: "
                        + task.getId() + ") no tiene un form_key asignado.");
            }
        }

        template.setStatus("PUBLISHED");
        templateRepo.save(template);
    }

    /**
     * Algoritmo de Kahn (Topological Sort) para detectar Ciclos (AC-3).
     */
    private void validateAcyclicDependencies(List<ProjectTemplateTreeDTO.DependencyDTO> dependencies) {
        if (dependencies == null || dependencies.isEmpty())
            return;

        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjList = new HashMap<>();

        // Populate graph variables
        for (ProjectTemplateTreeDTO.DependencyDTO edge : dependencies) {
            String u = edge.getSourceTaskId();
            String v = edge.getTargetTaskId();

            inDegree.putIfAbsent(u, 0);
            inDegree.putIfAbsent(v, 0);
            adjList.putIfAbsent(u, new ArrayList<>());

            adjList.get(u).add(v);
            inDegree.put(v, inDegree.get(v) + 1);
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int visitedNodes = 0;

        while (!queue.isEmpty()) {
            String u = queue.poll();
            visitedNodes++;

            if (adjList.containsKey(u)) {
                for (String v : adjList.get(u)) {
                    inDegree.put(v, inDegree.get(v) - 1);
                    if (inDegree.get(v) == 0) {
                        queue.add(v);
                    }
                }
            }
        }

        if (visitedNodes != inDegree.size()) {
            throw new IllegalArgumentException(
                    "Topological Error: Detectado un ciclo en las dependencias (Relación Circular).");
        }
    }
}
