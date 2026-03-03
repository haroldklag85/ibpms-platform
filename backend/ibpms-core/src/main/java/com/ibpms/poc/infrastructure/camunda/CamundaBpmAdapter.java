package com.ibpms.poc.infrastructure.camunda;

import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.HistoryService;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adaptador Driven — Motor BPM Camunda 7.
 * Implementa el puerto ProcesoBpmPort usando la API embebida de Camunda.
 * Hexagonally isolated: la capa de aplicación no importa nada de Camunda.
 */
@Component
public class CamundaBpmAdapter implements ProcesoBpmPort {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;

    public CamundaBpmAdapter(RuntimeService runtimeService,
            TaskService taskService,
            RepositoryService repositoryService,
            HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
    }

    @Override
    public String iniciarProceso(String definitionKey, String businessKey, Map<String, Object> variables) {
        try {
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(definitionKey, businessKey, variables);
            return instance.getId();
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar proceso BPM: " + e.getMessage(), e);
        }
    }

    @Override
    public void completarTarea(String camundaTaskId, Map<String, Object> variables) {
        taskService.complete(camundaTaskId, variables);
    }

    @Override
    public void suspenderProceso(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void desplegarDefinicion(String resourceName, byte[] bpmnContent) {
        repositoryService.createDeployment()
                .addString(resourceName, new String(bpmnContent, java.nio.charset.StandardCharsets.UTF_8))
                .enableDuplicateFiltering(true)
                .deploy();
    }

    @Override
    public void desplegarProceso(String resourceName, String xmlString) {
        try {
            repositoryService.createDeployment()
                    .addString(resourceName, xmlString)
                    .name("Despliegue Web Dinamico")
                    .enableDuplicateFiltering(true)
                    .deploy();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al desplegar modelo en el motor BPM: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> obtenerHistorialPorVariable(String variableName, String variableValue) {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals(variableName, variableValue)
                .list();

        return instances.stream().map(inst -> {
            Map<String, Object> map = new HashMap<>();
            map.put("processInstanceId", inst.getId());
            map.put("definitionKey", inst.getProcessDefinitionKey());
            map.put("businessKey", inst.getBusinessKey());
            map.put("state", inst.getState());
            map.put("startTime", inst.getStartTime());
            map.put("endTime", inst.getEndTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void reclamarTarea(String taskId, String username) {
        try {
            taskService.claim(taskId, username);
        } catch (org.camunda.bpm.engine.TaskAlreadyClaimedException e) {
            throw new com.ibpms.poc.domain.exception.TaskAlreadyClaimedException(
                    "La tarea " + taskId + " ya fue asignada a otro usuario.");
        }
    }

    @Override
    public void liberarTarea(String taskId, Map<String, Object> variables) {
        // En Camunda, liberar una tarea se logra seteando el assignee a null
        taskService.setAssignee(taskId, null);
        if (variables != null && !variables.isEmpty()) {
            taskService.setVariablesLocal(taskId, variables); // Actualizar como Draft
        }
    }

    @Override
    public void reasignarTarea(String taskId, String newUserId) {
        taskService.setAssignee(taskId, newUserId);
    }
}
