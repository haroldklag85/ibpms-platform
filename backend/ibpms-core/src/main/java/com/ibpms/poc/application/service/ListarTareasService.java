package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.TaskDTO;
import com.ibpms.poc.application.port.in.ListarTareasUseCase;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación — Listar Tareas (Inbox).
 * Utiliza la API nativa de Camunda (TaskService) delegando la búsqueda al
 * motor.
 */
@Service
public class ListarTareasService implements ListarTareasUseCase {

    private final TaskService taskService;

    public ListarTareasService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> listar(int limit, int offset, String status, Integer priority) {
        TaskQuery query = taskService.createTaskQuery();

        if (priority != null) {
            query.taskPriority(priority);
        }

        // Camunda native sorting
        query.orderByTaskCreateTime().desc();

        List<Task> tareasCamunda = query.listPage(offset, limit);

        return tareasCamunda.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TaskDTO toDto(Task t) {
        TaskDTO dto = new TaskDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setCaseId(t.getProcessInstanceId()); // Vínculo Expediente <-> Proceso
        dto.setAssignee(t.getAssignee());
        dto.setPriority(t.getPriority());

        if (t.getCreateTime() != null) {
            dto.setCreatedAt(t.getCreateTime().toInstant().toString());
        }
        if (t.getDueDate() != null) {
            dto.setDueDate(t.getDueDate().toInstant().toString());
        }
        return dto;
    }
}
