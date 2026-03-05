package com.ibpms.core.project.service;

import com.ibpms.core.project.domain.ProjectBaseline;
import com.ibpms.core.project.domain.ProjectTaskExecution;
import com.ibpms.core.project.dto.AssignTaskDTO;
import com.ibpms.core.project.dto.ProjectTaskExecutionDTO;
import com.ibpms.core.project.repository.ProjectBaselineRepository;
import com.ibpms.core.project.repository.ProjectTaskExecutionRepository;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GanttExecutionService {

    private static final Logger log = LoggerFactory.getLogger(GanttExecutionService.class);

    private final ProjectBaselineRepository baselineRepository;
    private final ProjectTaskExecutionRepository taskExecutionRepository;
    private final RuntimeService runtimeService;

    public GanttExecutionService(ProjectBaselineRepository baselineRepository,
            ProjectTaskExecutionRepository taskExecutionRepository,
            RuntimeService runtimeService) {
        this.baselineRepository = baselineRepository;
        this.taskExecutionRepository = taskExecutionRepository;
        this.runtimeService = runtimeService;
    }

    public List<ProjectTaskExecutionDTO> getGanttTree(String projectId) {
        return taskExecutionRepository.findByProjectId(projectId).stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignTask(String taskId, AssignTaskDTO dto) {
        ProjectTaskExecution task = taskExecutionRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task ID No Encontrado"));

        task.setAssigneeUserId(dto.getAssigneeUserId());
        task.setActualBudget(dto.getActualBudget());
        taskExecutionRepository.save(task);
    }

    /**
     * AC-2 (El "Big Bang" de Camunda - TRANSACCIONALIDAD V1):
     * Fija la línea base e invoca el Java API local de Camunda para instanciar las
     * primeras tareas.
     */
    @Transactional
    public String freezeBaseline(String projectId) {
        log.info("Iniciando Big Bang Transaccional para el proyecto: {}", projectId);

        // 1. Crear Línea Base
        ProjectBaseline baseline = new ProjectBaseline();
        baseline.setProjectId(projectId);
        baseline.setTotalBudget(BigDecimal.ZERO); // Aca se sumaria el costo total del arbol real
        baseline.setCreatedBy("current_pm_user"); // Mock Auth
        baselineRepository.save(baseline);

        // 2. Evaluar Primeras Tareas (Mock: En V1 buscamos las que no tienen
        // predecesores)
        List<ProjectTaskExecution> tasks = taskExecutionRepository.findByProjectId(projectId);

        // 3. INVOCAR directamente el Java API local de Camunda. SI ESTO FALLA, hay
        // ROLLBACK en MySQL.
        for (ProjectTaskExecution task : tasks) {
            if ("PENDING".equals(task.getStatus()) /* && task.hasNoDependencies() */) {
                log.info("Iniciando instanciación en Camunda para Tarea de Gantt: {}", task.getId());

                // startProcessInstanceByKey. Aqui 'gantt_task_execution' debe ser el Process ID
                // definido en el bpmn designer
                ProcessInstance instance = runtimeService.startProcessInstanceByKey("gantt_task_execution");

                task.setCamundaProcessInstanceId(instance.getId());
                task.setStatus("IN_PROGRESS");
                taskExecutionRepository.save(task);
            }
        }

        return baseline.getId();
    }

    private ProjectTaskExecutionDTO toDto(ProjectTaskExecution entity) {
        ProjectTaskExecutionDTO dto = new ProjectTaskExecutionDTO();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProjectId());
        dto.setWbsTaskTemplateId(entity.getWbsTaskTemplateId());
        dto.setStatus(entity.getStatus());
        dto.setAssigneeUserId(entity.getAssigneeUserId());
        dto.setActualBudget(entity.getActualBudget());
        dto.setStartDatePlan(entity.getStartDatePlan());
        dto.setEndDatePlan(entity.getEndDatePlan());
        return dto;
    }
}
