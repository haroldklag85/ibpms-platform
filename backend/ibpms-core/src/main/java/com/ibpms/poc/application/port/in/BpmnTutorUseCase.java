package com.ibpms.poc.application.port.in;

import com.ibpms.poc.infrastructure.web.dto.BpmnAnalysisResultDTO;

public interface BpmnTutorUseCase {
    /**
     * Evalúa la semántica de un proceso BPMN usando IA, enfocándose en ISO 9001 o
     * mejores prácticas.
     * 
     * @param xmlPayload XML del modelo BPMN
     * @param strictMode Activa la regla estricta de validación ISO 9001
     * @return DTO con hallazgos para que la UI mapee overlays sobre el Canvas
     */
    BpmnAnalysisResultDTO evaluateProcess(String xmlPayload, boolean strictMode);
}
