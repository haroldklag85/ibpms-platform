package com.ibpms.poc.application.port.out;

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
}
