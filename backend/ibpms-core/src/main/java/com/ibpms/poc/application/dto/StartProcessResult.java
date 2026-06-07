package com.ibpms.poc.application.dto;

import java.time.Instant;

/**
 * DTO inmutable de respuesta para el inicio de una instancia de proceso BPMN.
 * Encapsula los datos retornados al Frontend tras una ejecución exitosa.
 *
 * @param processInstanceId   ID de la instancia creada en Camunda
 * @param processDefinitionKey Clave de la definición de proceso ejecutada
 * @param businessKey          Clave de negocio asociada (opcional)
 * @param startedAt            Timestamp UTC del inicio
 * @param startedBy            Username del usuario que inició el proceso
 *
 * @Traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal)
 */
public record StartProcessResult(
        String processInstanceId,
        String processDefinitionKey,
        String businessKey,
        Instant startedAt,
        String startedBy
) {
}
