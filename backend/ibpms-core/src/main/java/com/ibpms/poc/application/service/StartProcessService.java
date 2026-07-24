package com.ibpms.poc.application.service;

import com.ibpms.poc.application.dto.StartProcessResult;
import com.ibpms.poc.application.port.in.StartProcessUseCase;
import com.ibpms.poc.application.port.out.ProcesoBpmPort;
import com.ibpms.poc.domain.exception.ProcessDefinitionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de Aplicación: Orquesta el inicio de una instancia de proceso BPMN.
 * Valida la existencia de la definición de proceso y delega al puerto de salida.
 *
 * <p>Flujo:
 * 1. Valida que el processDefinitionKey existe en Camunda (RepositoryService).
 * 2. Inyecta variable de trazabilidad del iniciador.
 * 3. Delega a {@link ProcesoBpmPort#iniciarProceso} (Camunda embebido).
 * 4. Retorna el resultado encapsulado.
 *
 * @Traceability US-007 — Ejecución BPMN, ADR-001 (Hexagonal), ADR-003 (Camunda Embedded)
 */
@Service
@Slf4j
public class StartProcessService implements StartProcessUseCase {

    private final ProcesoBpmPort procesoBpmPort;
    private final RepositoryService repositoryService;

    public StartProcessService(ProcesoBpmPort procesoBpmPort,
                               RepositoryService repositoryService) {
        this.procesoBpmPort = procesoBpmPort;
        this.repositoryService = repositoryService;
    }

    @Override
    @Transactional
    public StartProcessResult start(String processDefinitionKey,
                                    String businessKey,
                                    Map<String, Object> variables,
                                    String initiatorUsername) {

        log.info("Iniciando proceso BPMN [key={}, businessKey={}, initiator={}]",
                processDefinitionKey, businessKey, initiatorUsername);

        // 1. Validar existencia de la definición de proceso en el motor
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult();

        if (definition == null) {
            log.warn("Definición de proceso no encontrada [key={}]", processDefinitionKey);
            throw new ProcessDefinitionNotFoundException(processDefinitionKey);
        }

        // 2. Preparar variables con trazabilidad del iniciador
        Map<String, Object> enrichedVariables = variables != null
                ? new HashMap<>(variables)
                : new HashMap<>();
        enrichedVariables.put("ibpms_initiator_id", initiatorUsername);

        // 3. Delegar al puerto de salida (CamundaBpmAdapter)
        String processInstanceId = procesoBpmPort.iniciarProceso(
                processDefinitionKey,
                businessKey,
                enrichedVariables);

        log.info("Proceso BPMN iniciado exitosamente [instanceId={}, key={}]",
                processInstanceId, processDefinitionKey);

        // 4. Construir resultado inmutable
        return new StartProcessResult(
                processInstanceId,
                processDefinitionKey,
                businessKey,
                Instant.now(),
                initiatorUsername);
    }
}
