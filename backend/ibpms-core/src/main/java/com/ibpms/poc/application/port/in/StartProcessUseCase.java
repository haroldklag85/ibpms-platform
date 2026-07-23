package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.StartProcessResult;
import java.util.Map;

/**
 * Puerto de Entrada: Iniciar una instancia de proceso BPMN de forma autenticada.
 * Abstracción hexagonal que desacopla el controlador REST del motor Camunda.
 *
 * @Traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal), ADR-003 (Camunda Embedded)
 */
public interface StartProcessUseCase {

    /**
     * Inicia una nueva instancia de proceso BPMN en el motor embebido.
     *
     * @param processDefinitionKey Clave técnica del proceso desplegado en Camunda
     * @param businessKey          Clave de negocio para correlación (puede ser null)
     * @param variables            Variables de proceso a inyectar al arranque
     * @param initiatorUsername    Username del usuario autenticado que inicia el proceso
     * @return Resultado con el ID de la instancia creada y metadata
     * @throws com.ibpms.poc.domain.exception.ProcessDefinitionNotFoundException
     *         si la clave de proceso no existe en el motor
     */
    StartProcessResult start(String processDefinitionKey,
                             String businessKey,
                             Map<String, Object> variables,
                             String initiatorUsername);
}
