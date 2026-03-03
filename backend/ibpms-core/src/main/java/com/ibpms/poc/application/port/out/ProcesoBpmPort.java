package com.ibpms.poc.application.port.out;

import java.util.List;
import java.util.Map;

/**
 * Puerto de Salida: Motor BPM (Camunda 7).
 * Abstracción Hexagonal que aísla al dominio de la API de Camunda.
 * Facilita la migración a Camunda 8 u otro motor en V2 (ADR-003).
 */
public interface ProcesoBpmPort {

    /**
     * Instancia un proceso BPMN en el motor a partir de su ID de definición.
     * Retorna el ProcessInstanceId o lanza excepción.
     */
    String iniciarProceso(String definitionKey, String businessKey, Map<String, Object> variables);

    /**
     * Toma un archivo BPMN o DMN crudo en formato XML (String) y lo inyecta
     * caliente en el Repository Service de Camunda.
     * 
     * @param resourceName Nombre del archivo (Ej. "proceso_credito.bpmn")
     * @param xmlString    Contenido as XML String
     */
    void desplegarProceso(String resourceName, String xmlString);

    /**
     * Completa una tarea de usuario en Camunda.
     * 
     * @param camundaTaskId ID de la UserTask en Camunda
     * @param variables     variables de cierre (formulario completado)
     */
    void completarTarea(String camundaTaskId, Map<String, Object> variables);

    /**
     * Suspende una instancia de proceso.
     * 
     * @param processInstanceId ID de la instancia en ejecución
     */
    void suspenderProceso(String processInstanceId);

    /**
     * Despliega un archivo BPMN al motor Camunda.
     * 
     * @param resourceName nombre del recurso (ej: "process.bpmn")
     * @param bpmnContent  bytes del archivo BPMN
     */
    void desplegarDefinicion(String resourceName, byte[] bpmnContent);

    /**
     * Consulta el historial de instancias de proceso filtrando por una variable.
     * 
     * @param variableName  Nombre de la variable (Ej. "crmId")
     * @param variableValue Valor de la variable
     * @return Lista de mapas con la metadata del proceso
     */
    List<Map<String, Object>> obtenerHistorialPorVariable(String variableName, String variableValue);

    /**
     * Reclama una tarea para un usuario, evitando problemas de concurrencia.
     * 
     * @param taskId   ID de la tarea
     * @param username Usuario al que se le asignará la tarea
     */
    void reclamarTarea(String taskId, String username);

    /**
     * Libera una tarea y actualiza sus variables locales para guardar como "Draft".
     * 
     * @param taskId    ID de la tarea
     * @param variables Payload parcial
     */
    void liberarTarea(String taskId, Map<String, Object> variables);

    /**
     * Reasigna directamente una tarea a otro usuario sin completar.
     * 
     * @param taskId    ID de la tarea
     * @param newUserId El nuevo usuario asignado
     */
    void reasignarTarea(String taskId, String newUserId);
}
