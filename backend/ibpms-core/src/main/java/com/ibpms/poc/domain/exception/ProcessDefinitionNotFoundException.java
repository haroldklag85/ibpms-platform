package com.ibpms.poc.domain.exception;

/**
 * Excepción de dominio lanzada cuando se intenta iniciar un proceso BPMN
 * cuya definición (processDefinitionKey) no existe en el motor Camunda.
 *
 * @Traceability US-007 — Ejecución BPMN (Hexagonal Architecture)
 */
public class ProcessDefinitionNotFoundException extends RuntimeException {

    private final String processDefinitionKey;

    public ProcessDefinitionNotFoundException(String processDefinitionKey) {
        super("No se encontró la definición de proceso con la clave: " + processDefinitionKey);
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }
}
