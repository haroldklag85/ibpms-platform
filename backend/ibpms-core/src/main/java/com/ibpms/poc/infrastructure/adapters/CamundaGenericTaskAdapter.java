package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.GenericTaskPort;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador de infraestructura para Camunda TaskService orientado al Formulario Genérico.
 * Requerimiento: US-039 (Formulario Genérico Dinámico)
 * Permite cumplir CA-2, CA-8 (Extracción de variables, Completitud y Validaciones implícitas)
 */
@Component
public class CamundaGenericTaskAdapter implements GenericTaskPort {

    private final TaskService taskService;

    public CamundaGenericTaskAdapter(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean taskExists(String taskId) {
        return getTask(taskId) != null;
    }

    @Override
    public String getTaskAssignee(String taskId) {
        Task task = getTask(taskId);
        return task != null ? task.getAssignee() : null;
    }

    @Override
    public String getProcessDefinitionId(String taskId) {
        Task task = getTask(taskId);
        return task != null ? task.getProcessDefinitionId() : null;
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    @Override
    public void handleTaskBpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables) {
        taskService.handleBpmnError(taskId, errorCode, errorMessage, variables);
    }

    private Task getTask(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }
}
