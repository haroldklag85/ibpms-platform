package com.ibpms.poc.application.port.in;

import java.util.Map;

public interface GenerarPdfOficialUseCase {
    /**
     * Toma el contexto de variables finales del caso y genera un PDF inmutable,
     * inyectándolo luego en la bóveda documental (Azure).
     *
     * @param caseId    UUID del Expediente
     * @param variables Variables finales aprobadas
     * @param author    Usuario o sistema que generó el cierre
     */
    void generarPdfCierre(String caseId, Map<String, Object> variables, String author);
}
