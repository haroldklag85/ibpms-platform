package com.ibpms.poc.application.port.in;

import com.ibpms.poc.application.dto.DmnTranslationRequestDTO;
import com.ibpms.poc.application.dto.DmnTranslationResponseDTO;

public interface GenerarDmnAiUseCase {
    /**
     * Genera un esquema DMN 1.3 en base a solicitudes en lenguaje natural.
     * Retorna el Payload DMN crudo dentro del DTO de respuesta.
     */
    DmnTranslationResponseDTO generarDesdeTexto(DmnTranslationRequestDTO request);
}
