package com.ibpms.poc.application.ports.out;

import java.util.Map;

/**
 * Outbound Port para la gestión de tareas relacionadas con el formulario genérico.
 * Desacopla la lógica de negocio de la API de Camunda.
 * Requerimiento: US-039 (Formulario Genérico Dinámico)
 * Soportando los Criterios de Aceptación para obtención de variables y completitud (CA-2, CA-8).
 */
public interface GenericTaskPort {
    boolean taskExists(String taskId);
    String getTaskAssignee(String taskId);
    String getProcessDefinitionId(String taskId);
    Map<String, Object> getTaskVariables(String taskId);
    void completeTask(String taskId, Map<String, Object> variables);
    void handleTaskBpmnError(String taskId, String errorCode, String errorMessage, Map<String, Object> variables);
}
