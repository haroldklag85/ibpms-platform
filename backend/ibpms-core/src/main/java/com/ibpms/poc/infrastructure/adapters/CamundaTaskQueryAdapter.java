package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.TaskQueryPort;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de infraestructura para acceder a tareas de Camunda.
 * @Traceability: US-029 - Aislar dependencias directas a Camunda TaskService.
 */
@Component
public class CamundaTaskQueryAdapter implements TaskQueryPort {

    private final TaskService taskService;

    public CamundaTaskQueryAdapter(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Optional<TaskInfo> findTaskById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task != null) {
            return Optional.of(new TaskInfo(task.getId(), task.getProcessInstanceId(), task.getAssignee()));
        }
        return Optional.empty();
    }
}
