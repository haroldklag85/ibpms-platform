package com.ibpms.dmn;

import com.ibpms.dmn.DmnTranslationRequestDTO;
import com.ibpms.dmn.DmnTranslationResponseDTO;

public interface GenerarDmnAiUseCase {
    /**
     * Genera un esquema DMN 1.3 en base a solicitudes en lenguaje natural.
     * Retorna el Payload DMN crudo dentro del DTO de respuesta.
     */
    DmnTranslationResponseDTO generarDesdeTexto(DmnTranslationRequestDTO request);
}
