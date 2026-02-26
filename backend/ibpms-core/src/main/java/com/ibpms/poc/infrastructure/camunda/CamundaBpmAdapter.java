package com.ibpms.poc.infrastructure.camunda;

import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.Map;

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

    public CamundaBpmAdapter(RuntimeService runtimeService,
            TaskService taskService,
            RepositoryService repositoryService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
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
                .addBytes(resourceName, bpmnContent)
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
}
