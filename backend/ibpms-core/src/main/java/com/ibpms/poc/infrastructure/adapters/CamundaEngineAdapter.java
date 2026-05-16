package com.ibpms.poc.infrastructure.adapters;

import com.ibpms.poc.application.ports.out.ProcessEnginePort;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para el motor Camunda.
 * Encapsula la comunicación con la API de Camunda para mantener la capa
 * de aplicación puramente enfocada en el dominio (Arquitectura Hexagonal).
 * Requerimiento: US-003 (iForm Maestro - Formularios Dinámicos)
 * Proveedor de validación cruzada para: CA-26 (Bloqueo de borrado de formularios activos).
 */
@Component
public class CamundaEngineAdapter implements ProcessEnginePort {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public CamundaEngineAdapter(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    @Override
    public long countActiveProcessInstances() {
        return runtimeService.createProcessInstanceQuery().active().count();
    }

    @Override
    public long countActiveTasksWithForm() {
        return historyService.createHistoricTaskInstanceQuery().unfinished().count();
    }
}
