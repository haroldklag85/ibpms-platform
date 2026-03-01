package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.AiMetricsDTO;
import com.ibpms.poc.application.dto.ProcessHealthDTO;

/**
 * Puertos de Entrada para la extracción de analítica de gerencia (Dashboards).
 */
public interface ObtenerMetricasUseCase {

    /**
     * Calcula métricas de volumen transaccional del motor Camunda.
     */
    ProcessHealthDTO getProcessHealth();

    /**
     * Calcula métricas de impacto de la IA basada en AiAuditLogEntity.
     */
    AiMetricsDTO getAiMetrics();
}
