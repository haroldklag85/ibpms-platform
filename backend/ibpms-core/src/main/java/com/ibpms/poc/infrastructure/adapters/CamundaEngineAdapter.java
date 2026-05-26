package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.ProcessEnginePort;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de infraestructura para el motor Camunda.
 * Encapsula la comunicación con la API de Camunda para mantener la capa
 * de aplicación puramente enfocada en el dominio (Arquitectura Hexagonal).
 * Requerimiento: US-003 (iForm Maestro - Formularios Dinámicos)
 * Proveedor de validación cruzada para: CA-26 (Bloqueo de borrado de formularios activos).
 */
// @Traceability: US-005, CA-26
@Component
public class CamundaEngineAdapter implements ProcessEnginePort {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;

    public CamundaEngineAdapter(RuntimeService runtimeService, HistoryService historyService, RepositoryService repositoryService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.taskService = taskService;
    }

    @Override
    public long countActiveProcessInstances(String formKey) {
        List<org.camunda.bpm.engine.runtime.ProcessInstance> activeInstances = runtimeService.createProcessInstanceQuery().active().list();
        long count = 0;
        for (org.camunda.bpm.engine.runtime.ProcessInstance instance : activeInstances) {
            String processDefinitionId = instance.getProcessDefinitionId();
            try {
                BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
                if (modelInstance != null) {
                    for (UserTask userTask : modelInstance.getModelElementsByType(UserTask.class)) {
                        if (formKey != null && formKey.equals(userTask.getCamundaFormKey())) {
                            count++;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Si hay algún problema leyendo el modelo BPMN, continuamos
            }
        }
        return count;
    }

    @Override
    public long countActiveTasksWithForm(String formKey) {
        List<org.camunda.bpm.engine.task.Task> tasks = taskService.createTaskQuery().initializeFormKeys().list();
        long count = 0;
        for (org.camunda.bpm.engine.task.Task task : tasks) {
            if (formKey != null && formKey.equals(task.getFormKey())) {
                count++;
            }
        }
        return count;
    }
}
