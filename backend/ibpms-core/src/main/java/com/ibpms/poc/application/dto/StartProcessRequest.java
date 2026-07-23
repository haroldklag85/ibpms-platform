package com.ibpms.poc.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * DTO de entrada para iniciar una instancia de proceso BPMN.
 * Validado con Jakarta Bean Validation en el Controller.
 *
 * @param processDefinitionKey Clave técnica del proceso desplegado en Camunda (obligatorio)
 * @param businessKey          Clave de negocio para correlación (opcional)
 * @param variables            Variables de proceso a inyectar al arranque (opcional)
 *
 * @Traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal)
 */
public record StartProcessRequest(
        @NotBlank(message = "La clave de definición de proceso (processDefinitionKey) es obligatoria.")
        String processDefinitionKey,

        String businessKey,

        Map<String, Object> variables
) {
}
